package com.teamsoft.ms.auth.service;

import com.teamsoft.ms.auth.entities.Token;
import com.teamsoft.ms.auth.model.request.LoginRequest;
import com.teamsoft.ms.auth.model.request.RegisterRequest;
import com.teamsoft.ms.auth.model.response.TokenResponse;
import com.teamsoft.ms.auth.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(tokenRepository, passwordEncoder, jwtService, authenticationManager);
        ReflectionTestUtils.setField(authService, "jwtExpiration", 60_000L);
        ReflectionTestUtils.setField(authService, "refreshExpiration", 120_000L);
        ReflectionTestUtils.setField(authService, "absoluteSessionExpiration", 7);

        lenient().when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "ENC_" + invocation.getArgument(0));
        lenient().when(jwtService.generateToken(any(), anyString()))
                .thenAnswer(invocation -> "access-" + invocation.getArgument(1));
        lenient().when(jwtService.generateRefreshToken(any(), anyString()))
                .thenAnswer(invocation -> "refresh-" + invocation.getArgument(1));
        lenient().when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(tokenRepository.saveAll(anyCollection()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void register_shouldGenerateTokensAndPersistBoth() {
        RegisterRequest request = buildRegisterRequest();

        TokenResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).startsWith("access-");
        assertThat(response.getRefreshToken()).startsWith("refresh-");

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository, times(2)).save(captor.capture());
        List<Token> savedTokens = captor.getAllValues();

        assertThat(savedTokens).hasSize(2);
        assertThat(savedTokens)
                .extracting(Token::getTokenType)
                .containsExactly(Token.TokenType.ACCESS, Token.TokenType.REFRESH);
        savedTokens.forEach(token -> {
            assertThat(token.getUserId()).isEqualTo("123");
            assertThat(token.isRevoked()).isFalse();
            assertThat(token.getExpiresAt()).isAfter(Instant.now().minusSeconds(1));
            assertThat(token.getAbsoluteExpiresAt()).isAfter(Instant.now().minusSeconds(1));
        });
    }

    @Test
    void login_shouldAuthenticateRevokeExistingAndPersistNewTokens() {
        LoginRequest request = buildLoginRequest();
        Token existingToken = Token.builder()
                .jti("existing-jti")
                .tokenType(Token.TokenType.ACCESS)
                .revoked(false)
                .userId("123")
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .absoluteExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        when(tokenRepository.findAllValidTokensByUser(eq("123"), eq(Token.TokenType.ACCESS), any(Instant.class)))
                .thenReturn(List.of(existingToken));

        TokenResponse response = authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        UsernamePasswordAuthenticationToken authentication = authCaptor.getValue();
        assertThat(authentication.getPrincipal()).isEqualTo(request.email);
        assertThat(authentication.getCredentials()).isEqualTo(request.password);

        assertThat(existingToken.isRevoked()).isTrue();
        verify(tokenRepository).saveAll(argThat(tokens -> {
            for (Token token : tokens) {
                if (token == existingToken) {
                    return true;
                }
            }
            return false;
        }));

        assertThat(response.getAccessToken()).startsWith("access-");
        assertThat(response.getRefreshToken()).startsWith("refresh-");

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository, times(2)).save(captor.capture());
        List<Token> savedTokens = captor.getAllValues();
        assertThat(savedTokens)
                .extracting(Token::getTokenType)
                .containsExactly(Token.TokenType.ACCESS, Token.TokenType.REFRESH);
    }

    @Test
    void refresh_withValidBearerShouldRevokeOldAndIssueNewTokens() {
        String oldRefreshToken = "old-refresh-token";
        String bearer = "Bearer " + oldRefreshToken;
        TaggableToken storedRefresh = new TaggableToken(Token.builder()
                .jti("old-refresh-jti")
                .tokenType(Token.TokenType.REFRESH)
                .revoked(false)
                .userId("123")
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .absoluteExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build());

        when(jwtService.extractJti(oldRefreshToken)).thenReturn("old-refresh-jti");
        when(jwtService.isTokenValid(eq(oldRefreshToken), any())).thenReturn(true);
        when(tokenRepository.findValidToken(eq("old-refresh-jti"), eq(Token.TokenType.REFRESH), any(Instant.class)))
                .thenReturn(Optional.of(storedRefresh.token));
        when(tokenRepository.findAllValidTokensByUser(eq("123"), eq(Token.TokenType.ACCESS), any(Instant.class)))
                .thenReturn(List.of());

        TokenResponse response = authService.refresh(bearer);

        assertThat(storedRefresh.token.isRevoked()).isTrue();
        verify(tokenRepository).save(storedRefresh.token);
        verify(tokenRepository).saveAll(anyCollection());

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository, times(3)).save(captor.capture());
        List<Token> savedTokens = captor.getAllValues();
        assertThat(savedTokens.get(0)).isSameAs(storedRefresh.token);
        assertThat(savedTokens.subList(1, 3))
                .extracting(Token::getTokenType)
                .containsExactly(Token.TokenType.ACCESS, Token.TokenType.REFRESH);

        assertThat(response.getAccessToken()).startsWith("access-");
        assertThat(response.getRefreshToken()).startsWith("refresh-");
    }

    @Test
    void refresh_withInvalidBearerHeaderShouldFailFast() {
        assertThatThrownBy(() -> authService.refresh("Token xyz"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Bearer Token");

        verifyNoInteractions(jwtService, tokenRepository, authenticationManager);
    }

    @Test
    void refresh_withInvalidRefreshTokenShouldThrow() {
        String bearer = "Bearer invalid";
        when(jwtService.extractJti("invalid")).thenReturn("jti-invalid");
        when(jwtService.isTokenValid(eq("invalid"), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(bearer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Refresh Token");

        verify(tokenRepository, never()).findValidToken(anyString(), any(), any());
    }

    @Test
    void refresh_withAbsoluteSessionExpiredShouldThrow() {
        String oldRefreshToken = "expired-refresh-token";
        String bearer = "Bearer " + oldRefreshToken;
        Token storedRefresh = Token.builder()
                .jti("expired-refresh-jti")
                .tokenType(Token.TokenType.REFRESH)
                .revoked(false)
                .userId("123")
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .absoluteExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(jwtService.extractJti(oldRefreshToken)).thenReturn("expired-refresh-jti");
        when(jwtService.isTokenValid(eq(oldRefreshToken), any())).thenReturn(true);
        when(tokenRepository.findValidToken(eq("expired-refresh-jti"), eq(Token.TokenType.REFRESH), any(Instant.class)))
                .thenReturn(Optional.of(storedRefresh));

        assertThatThrownBy(() -> authService.refresh(bearer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Session expired (absolute)");

        verify(tokenRepository, never()).save(any(Token.class));
        verify(tokenRepository, never()).saveAll(anyCollection());
    }

    private RegisterRequest buildRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.documentNumber = "123456";
        request.documentType = "CC";
        request.name = "John";
        request.lastName = "Doe";
        request.email = "john.doe@example.com";
        request.password = "secret";
        request.phone = "555";
        request.rol = "ADMIN";
        return request;
    }

    private LoginRequest buildLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.email = "john.doe@example.com";
        request.password = "secret";
        return request;
    }

    private static class TaggableToken {
        private final Token token;

        private TaggableToken(Token token) {
            this.token = token;
        }
    }
}
