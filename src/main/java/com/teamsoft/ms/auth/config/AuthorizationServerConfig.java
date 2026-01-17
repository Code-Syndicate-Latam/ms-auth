package com.teamsoft.ms.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@RequiredArgsConstructor
public class AuthorizationServerConfig {

    private final DataSource dataSource;
    @Value("${jwt.keystore.path}")
    private Resource keystore;

    @Value("${jwt.keystore.password}")
    private String storePassword;

    @Value("${jwt.keystore.key-alias}")
    private String keyAlias;

    @Value("${jwt.keystore.key-password}")
    private String keyPassword;

    @Value("${server.uri}")
    private String serverUri;

    @Value("${jwt.kid}")
    private String kid;

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(new JdbcTemplate(dataSource), registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(new JdbcTemplate(dataSource), registeredClientRepository);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer
                                .oidc(Customizer.withDefaults())
                )
                .authorizeHttpRequests((authorize) ->
                        authorize.anyRequest().authenticated())
                //.csrf(csrf -> csrf.disable())
                //.formLogin(Customizer.withDefaults())
        ;

        return http.formLogin(Customizer.withDefaults()).build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/login", "/error")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = loadRsaFromKeystore();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private RSAKey loadRsaFromKeystore() {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = keystore.getInputStream()) {
                ks.load(is, storePassword.toCharArray());
            }

            Key key = ks.getKey(keyAlias, keyPassword.toCharArray());
            RSAPrivateKey privateKey = (RSAPrivateKey) key;

            Certificate cert = ks.getCertificate(keyAlias);
            RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(kid)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load RSA keys from keystore", e);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(serverUri)
                .build();
    }
}
