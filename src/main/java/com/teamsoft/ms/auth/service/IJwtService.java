package com.teamsoft.ms.auth.service;

import com.teamsoft.ms.auth.model.dto.UserDto;

import java.util.Date;

public interface IJwtService {
    String generateToken(UserDto user, String jti);
    String extractUsername(String token);
    String extractJti(String token);
    Date extractExpiration(String token);
    boolean isTokenValid(String token, UserDto user);
    boolean tokenIsExpired(String token);
    String generateRefreshToken(UserDto user, String jti);
    String buildToken(UserDto user, Long expiration, String jti);
}

