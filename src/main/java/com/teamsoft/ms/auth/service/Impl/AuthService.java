package com.teamsoft.ms.auth.service.Impl;


import com.teamsoft.ms.auth.entities.Token;
import com.teamsoft.ms.auth.entities.User;
import com.teamsoft.ms.auth.exception.RoleNotFoundException;
import com.teamsoft.ms.auth.model.dto.UserDto;
import com.teamsoft.ms.auth.model.request.LoginRequest;
import com.teamsoft.ms.auth.model.request.RegisterRequest;
import com.teamsoft.ms.auth.model.request.external.CreateUserRequest;
import com.teamsoft.ms.auth.model.response.TokenResponse;
import com.teamsoft.ms.auth.repository.TokenRepository;
import com.teamsoft.ms.auth.repository.UserRepository;
import com.teamsoft.ms.auth.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    @Value("${jwt.expiration-milliseconds}")
    private Long jwtExpiration;
    @Value("${jwt.refresh-token.expiration-milliseconds}")
    private Long refreshExpiration;
    @Value("${jwt.absolute-session.expiration-days}")
    private int absoluteSessionExpiration;

    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public TokenResponse register(RegisterRequest req){
        var createUserReq = CreateUserRequest.builder()
                .nombre(req.name)
                .email(req.email)
                .rol(req.rol)
                .numeroDocumento(req.documentNumber)
                .tipoDocumento(req.documentType)
                .apellido(req.lastName)
                .telefono(req.phone)
                .passwordHash(passwordEncoder.encode(req.password))
                .build();
        // Llamar a creacion de usuario ms-usuarios si se desea

        Long roleId = parseRoleId(req.rol);
        // Si se proporcionó roleId, validar que exista en la tabla role
        if (roleId == null || (roleId != null && !roleRepository.existsById(roleId))) {
            throw new RoleNotFoundException("Role not found: " + roleId);
        }

        // Persistir credenciales en BD local
        User user = User.builder()
                .email(req.email)
                .passwordHash(passwordEncoder.encode(req.password))
                .enabled(true)
                .createdAt(Instant.now())
                .roleId(roleId)
                .build();
        user = userRepository.save(user);

        // Construir UserDto para tokens
        var userDto = UserDto.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .hashedPassword(user.getPasswordHash())
                .role(user.getRoleId() != null ? user.getRoleId().toString() : null)
                .permissions(List.of())
                .build();

        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        Instant absoluteExpiresAt = Instant.now().plus(absoluteSessionExpiration, ChronoUnit.DAYS);

        Instant refreshExpiresAt = new Date(System.currentTimeMillis()+refreshExpiration).toInstant();
        Instant accessExpiresAt = new Date(System.currentTimeMillis()+jwtExpiration).toInstant();

        var jwtToken =jwtService.generateToken(userDto, accessJti);
        var refreshToken = jwtService.generateRefreshToken(userDto, refreshJti);
        saveUserToken(userDto.getUserId(), accessJti, Token.TokenType.ACCESS, accessExpiresAt, absoluteExpiresAt);
        saveUserToken(userDto.getUserId(), refreshJti, Token.TokenType.REFRESH, refreshExpiresAt, absoluteExpiresAt);

        return new TokenResponse(jwtToken, refreshToken);
    }

    private Long parseRoleId(String rol){
        if (rol == null) return null;
        try{
            return Long.parseLong(rol);
        }catch(NumberFormatException ex){
            return null;
        }
    }

    public void saveUserToken(String userId, String jti, Token.TokenType tokenType, Instant expiresAt, Instant absoluteExpirationAt){

        var token = Token.builder()
                .userId(userId)
                .jti(jti)
                .tokenType(tokenType)
                .revoked(false)
                .expiresAt(expiresAt)
                .absoluteExpiresAt(absoluteExpirationAt)
                .build();
        tokenRepository.save(token);
    }
    @Transactional
    public TokenResponse refresh(final String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Bearer Token");
        }

        final String oldRefreshToken = authHeader.substring(7);
        final String refreshJti = jwtService.extractJti(oldRefreshToken);

        // 1. Validar JWT
        // obtener stored refresh token para validar userId y jti
        Token storedRefresh = tokenRepository
                .findValidToken(refreshJti, Token.TokenType.REFRESH, Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Refresh expired or revoked"));

        // 2. Recuperar usuario desde BD usando userId guardado en token
        final String userIdStr = storedRefresh.getUserId();
        final User user = userRepository.findById(Long.valueOf(userIdStr))
                .orElseThrow(() -> new IllegalArgumentException("User not found for refresh"));

        if (!jwtService.isTokenValid(oldRefreshToken, buildUserDto(user))) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        if (storedRefresh.getAbsoluteExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Session expired (absolute)");
        }
        revokeAccessTokens(buildUserDto(user));
        // 3. Revocar refresh viejo
        storedRefresh.setRevoked(true);
        tokenRepository.save(storedRefresh);

        // 4. Crear nuevos tokens


        Instant refreshExpiresAt = new Date(System.currentTimeMillis()+refreshExpiration).toInstant();
        Instant accessExpiresAt = new Date(System.currentTimeMillis()+jwtExpiration).toInstant();

        String newAccessJti = UUID.randomUUID().toString();
        String newRefreshJti = UUID.randomUUID().toString();

        String accessToken = jwtService.generateToken(buildUserDto(user), newAccessJti);
        String refreshToken = jwtService.generateRefreshToken(buildUserDto(user), newRefreshJti);

        saveUserToken(buildUserDto(user).getUserId(), newAccessJti, Token.TokenType.ACCESS, accessExpiresAt, storedRefresh.getAbsoluteExpiresAt());
        saveUserToken(buildUserDto(user).getUserId(), newRefreshJti, Token.TokenType.REFRESH,  refreshExpiresAt, storedRefresh.getAbsoluteExpiresAt());

        return new TokenResponse(accessToken, refreshToken);
    }
    @Transactional
    public TokenResponse login(LoginRequest req){
        // Comprobar existencia del usuario primero para devolver UsernameNotFoundException si no existe
        var optionalUser = userRepository.findByEmail(req.email);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + req.email);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.email,
                            req.password
                    )
            );
        } catch (AuthenticationException ex) {
            // Si la autenticación falla (usuario existe pero credenciales inválidas)
            throw new BadCredentialsException("Invalid credentials");
        }
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();
        Instant absoluteExpiresAt = Instant.now().plus(absoluteSessionExpiration, ChronoUnit.DAYS);
        Instant refreshExpiresAt = new Date(System.currentTimeMillis()+refreshExpiration).toInstant();
        Instant accessExpiresAt = new Date(System.currentTimeMillis()+jwtExpiration).toInstant();

        // Recuperar usuario real desde BD
        var user = optionalUser.get();
        var userDto = buildUserDto(user);
        var jwtToken = jwtService.generateToken(userDto, accessJti);
        var refreshToken = jwtService.generateRefreshToken(userDto, refreshJti);
        revokeAccessTokens(userDto);
        saveUserToken(userDto.getUserId(), accessJti, Token.TokenType.ACCESS, accessExpiresAt, absoluteExpiresAt);
        saveUserToken(userDto.getUserId(), refreshJti, Token.TokenType.REFRESH, refreshExpiresAt, absoluteExpiresAt);
        return new TokenResponse(jwtToken, refreshToken);
    }

    public UserDto getUserTest(){
        return UserDto.builder()
                .userId("123")
                .role("1")
                .permissions(List.of("1"))
                .hashedPassword(passwordEncoder.encode("clave123"))
                .email("jaider@admin.com")
                .build();
    }

    private UserDto buildUserDto(User user){
        return UserDto.builder()
                .userId(user.getId().toString())
                .role(user.getRoleId() != null ? user.getRoleId().toString() : null)
                .permissions(List.of())
                .hashedPassword(user.getPasswordHash())
                .email(user.getEmail())
                .build();
    }

    private void revokeAccessTokens(UserDto user) {
        var tokens = tokenRepository
                .findAllValidTokensByUser(user.getUserId(), Token.TokenType.ACCESS, Instant.now());

        tokens.forEach(t -> t.setRevoked(true));
        tokenRepository.saveAll(tokens);
    }

}
