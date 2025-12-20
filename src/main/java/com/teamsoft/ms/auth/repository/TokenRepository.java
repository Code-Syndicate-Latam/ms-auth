package com.teamsoft.ms.auth.repository;

import com.teamsoft.ms.auth.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
//    Optional<Token> findByJtiAndTokenTypeAndExpiredFalseAndRevokedFalse(
//            String jti, Token.TokenType tokenType);
//
//    List<Token> findAllByUserIdAndTokenTypeAndExpiredFalseAndRevokedFalse(
//            String userId, Token.TokenType tokenType);

    @Query("SELECT t FROM Token t WHERE t.jti = :jti " +
            "AND t.tokenType = :tokenType " +
            "AND t.revoked = false " +
            "AND (t.expiresAt IS NULL OR t.expiresAt > :now)")
    Optional<Token> findValidToken(
            @Param("jti") String jti,
            @Param("tokenType") Token.TokenType tokenType,
            @Param("now") Instant now
    );

    @Query("SELECT t FROM Token t WHERE t.userId = :userId " +
            "AND t.tokenType = :tokenType " +
            "AND t.revoked = false " +
            "AND (t.expiresAt IS NULL OR t.expiresAt > :now)")
    List<Token> findAllValidTokensByUser(
            @Param("userId") String userId,
            @Param("tokenType") Token.TokenType tokenType,
            @Param("now") Instant now
    );

}