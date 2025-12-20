package com.teamsoft.ms.auth.service;

import com.teamsoft.ms.auth.model.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    @Value("${jwt.secret-key}")
    private String secretKey;
    @Value("${jwt.expiration-milliseconds}")
    private Long jwtExpiration;
    @Value("${jwt.refresh-token.expiration-milliseconds}")
    private Long refreshExpiration;

    public String generateToken(UserDto user){
        return buildToken(user, jwtExpiration);
    }
    public String extractUsername(final String token){
        final Claims jwtToken = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return jwtToken.getSubject();
    }
    public Date extractExpiration(final String token){
        final Claims jwtToken = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return jwtToken.getExpiration();
    }
    public boolean isTokenValid(final String token, final UserDto user){
        final String email = extractUsername(token);
        return (email.equals(user.getEmail()) && !tokenIsExpired(token));

    }
    public boolean tokenIsExpired(final String token){
        return extractExpiration(token).before(new Date());
    }
    public String generateRefreshToken(UserDto user){
        return  buildToken(user, refreshExpiration);


    }
    public String buildToken(final UserDto user, final Long expiration ){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());                 // String
        claims.put("permissions", user.getPermissions());
        return Jwts.builder()
            .id(user.getUserId())
            .subject(user.getEmail())
            .claims(claims)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis()+expiration))
            .signWith(getSignInKey())
            .compact();
    }
    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
