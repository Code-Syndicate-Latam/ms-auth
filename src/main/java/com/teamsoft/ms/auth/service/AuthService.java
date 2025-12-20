package com.teamsoft.ms.auth.service;


import com.teamsoft.ms.auth.entities.Token;
import com.teamsoft.ms.auth.entities.User;
import com.teamsoft.ms.auth.model.dto.UserDto;
import com.teamsoft.ms.auth.model.request.LoginRequest;
import com.teamsoft.ms.auth.model.request.RegisterRequest;
import com.teamsoft.ms.auth.model.response.TokenResponse;
import com.teamsoft.ms.auth.repository.TokenRepository;
import lombok.RequiredArgsConstructor;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
        var jwtToken =jwtService.generateToken(userDto);
        var refreshToken = jwtService.generateRefreshToken(userDto);
        this.saveUserToken(userDto.getUserId(),jwtToken);
        return new TokenResponse(jwtToken, refreshToken);
    }

    public void saveUserToken(String userId, String jwtToken){
        var token = Token.builder()
                .userId(userId)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
    public TokenResponse refresh(final String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer")){
            throw new IllegalArgumentException("Invalid Bearer Token");
        }
        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);
        if(userEmail == null){
            throw new IllegalArgumentException("Invalid Refresh Token");
        }
        final UserDto user = getUserTest();
        if(!jwtService.isTokenValid(refreshToken, user)){
            throw new IllegalArgumentException("Invalid Refresh Token");
        }
        final String accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user.getUserId(), accessToken);
        return new TokenResponse(accessToken, refreshToken);
    }
    public TokenResponse login(LoginRequest req){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.email,
                        req.password
                )
        );
        var user = getUserTest();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user.getUserId(), jwtToken);
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
    private void revokeAllUserTokens(UserDto user){
        final List<Token> validUserTokens = tokenRepository.findAll();
        if(!validUserTokens.isEmpty()){
            for(final Token token : validUserTokens){
                token.setExpired(true);
                token.setRevoked(true);

            }
            tokenRepository.saveAll(validUserTokens);
        }
    }

}
