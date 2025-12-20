package com.teamsoft.ms.auth.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token")
public class Token {
    public enum TokenType {
        ACCESS,
        REFRESH
    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "token_id")
    public UUID tokenId;

    @Column(nullable = false, unique = true)
    private String jti;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type")
    public TokenType tokenType;

    public boolean revoked;

    @Column(name = "user_id")
    public String userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Version
    private Long version;

    @Column(name = "absolute_expires_at", nullable = true)
    private Instant absoluteExpiresAt;
}
