package com.teamsoft.ms.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class RegisteredClientsConfig {
    @Value("${jwt.service-client.secret}")
    private String serviceClientSecret;

    @Value("${jwt.gateway-client.secret}")
    private String gatewayClientSecret;

    @Value("${jwt.callback.uri}")
    private String callbackUri;

    @Value("${jwt.logout.uri}")
    private String logoutUri;

    @Bean
    CommandLineRunner initRegisteredClients(RegisteredClientRepository repo) {
        return args -> {

            // 1) service-client (microservicio -> microservicio) - client_credentials
            if (repo.findByClientId("service-client") == null) {
                RegisteredClient serviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("service-client")
                        .clientSecret(new BCryptPasswordEncoder().encode(serviceClientSecret))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope("api")
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofMinutes(10))
                                .build())
                        .build();
                repo.save(serviceClient);
            }

            // 2) spa-client (Angular) - authorization_code + PKCE (public client)
            if (repo.findByClientId("spa-client") == null) {
                RegisteredClient spaClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("spa-client")
                        .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUri(callbackUri) // AJUSTA
                        .postLogoutRedirectUri(logoutUri)
                        .scope("openid")
                        .scope("profile")
                        .scope("api")
                        .clientSettings(ClientSettings.builder()
                                .requireProofKey(true)  // PKCE
                                .requireAuthorizationConsent(false)
                                .build())
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofMinutes(10))
                                .refreshTokenTimeToLive(Duration.ofDays(7))
                                .reuseRefreshTokens(false) // rotación
                                .build())
                        .build();
                repo.save(spaClient);
            }

            if (repo.findByClientId("gateway-client") == null) {
                RegisteredClient gatewayClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("gateway-client")
                        .clientSecret(new BCryptPasswordEncoder().encode(gatewayClientSecret))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope("introspect") // opcional, si implementas introspección/administración
                        .build();
                repo.save(gatewayClient);
            }
        };
    }
}

