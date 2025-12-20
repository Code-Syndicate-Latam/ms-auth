package com.teamsoft.ms.auth.service;


import com.teamsoft.ms.auth.entities.Token;
import com.teamsoft.ms.auth.entities.User;
import com.teamsoft.ms.auth.model.dto.UserDto;
import com.teamsoft.ms.auth.model.request.LoginRequest;
import com.teamsoft.ms.auth.model.request.RegisterRequest;
import com.teamsoft.ms.auth.model.response.TokenResponse;
import com.teamsoft.ms.auth.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    @Transactional
    public TokenResponse register(RegisterRequest req){
        var user = User.builder()
                .nombre(req.name)
                .email(req.email)
                .rol(req.rol)
                .numeroDocumento(req.documentNumber)
                .tipoDocumento(req.documentType)
                .apellido(req.lastName)
                .telefono(req.phone)
                .passwordHash(passwordEncoder.encode(req.password))
                .build();
        // Llamar a creacion de usuario ms-usuarios
        var userDto = getUserTest();
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
        final UserDto user = getUserTest();

        // 1. Validar JWT
        if (!jwtService.isTokenValid(oldRefreshToken, user)) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        // 2. Validar en BD
        Token storedRefresh = tokenRepository
                .findValidToken(refreshJti, Token.TokenType.REFRESH, Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Refresh expired or revoked"));
        if (storedRefresh.getAbsoluteExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Session expired (absolute)");
        }
        revokeAccessTokens(user);
        // 3. Revocar refresh viejo
        storedRefresh.setRevoked(true);
        tokenRepository.save(storedRefresh);

        // 4. Crear nuevos tokens


        Instant refreshExpiresAt = new Date(System.currentTimeMillis()+refreshExpiration).toInstant();
        Instant accessExpiresAt = new Date(System.currentTimeMillis()+jwtExpiration).toInstant();

        String newAccessJti = UUID.randomUUID().toString();
        String newRefreshJti = UUID.randomUUID().toString();

        String accessToken = jwtService.generateToken(user, newAccessJti);
        String refreshToken = jwtService.generateRefreshToken(user, newRefreshJti);

        saveUserToken(user.getUserId(), newAccessJti, Token.TokenType.ACCESS, accessExpiresAt, storedRefresh.getAbsoluteExpiresAt());
        saveUserToken(user.getUserId(), newRefreshJti, Token.TokenType.REFRESH,  refreshExpiresAt, storedRefresh.getAbsoluteExpiresAt());

        return new TokenResponse(accessToken, refreshToken);
    }
    @Transactional
    public TokenResponse login(LoginRequest req){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.email,
                        req.password
                )
        );
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();
        Instant absoluteExpiresAt = Instant.now().plus(absoluteSessionExpiration, ChronoUnit.DAYS);
        Instant refreshExpiresAt = new Date(System.currentTimeMillis()+refreshExpiration).toInstant();
        Instant accessExpiresAt = new Date(System.currentTimeMillis()+jwtExpiration).toInstant();

        var user = getUserTest();
        var jwtToken = jwtService.generateToken(user, accessJti);
        var refreshToken = jwtService.generateRefreshToken(user, refreshJti);
        revokeAccessTokens(user);
        saveUserToken(user.getUserId(), accessJti, Token.TokenType.ACCESS, accessExpiresAt, absoluteExpiresAt);
        saveUserToken(user.getUserId(), refreshJti, Token.TokenType.REFRESH, refreshExpiresAt, absoluteExpiresAt);
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
    private void revokeAccessTokens(UserDto user) {
        var tokens = tokenRepository
                .findAllValidTokensByUser(user.getUserId(), Token.TokenType.ACCESS, Instant.now());

        tokens.forEach(t -> t.setRevoked(true));
        tokenRepository.saveAll(tokens);
    }

}
