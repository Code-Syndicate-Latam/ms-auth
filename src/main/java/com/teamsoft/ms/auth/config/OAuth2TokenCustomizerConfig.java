package com.teamsoft.ms.auth.config;

import com.teamsoft.ms.auth.entities.Role;
import com.teamsoft.ms.auth.entities.User;
import com.teamsoft.ms.auth.repository.RoleRepository;
import com.teamsoft.ms.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;


import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class OAuth2TokenCustomizerConfig {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }

            String username = context.getPrincipal().getName();
            User user = userRepository.findByEmail(username).orElse(null);
            if (user == null) return;

            List<String> permissions = Collections.emptyList();
            if (user.getRoleId() != null) {
                permissions = roleRepository.findById(user.getRoleId())
                        .map(Role::getPermissions)
                        .orElse(Collections.emptyList());
            }

            context.getClaims()
                    .claim("user_id", user.getId())
                    .claim("role", user.getRoleId())
                    .claim("permissions", permissions);
        };
    }
}

