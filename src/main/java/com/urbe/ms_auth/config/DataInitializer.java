package com.urbe.ms_auth.config;

import com.urbe.ms_auth.entity.PermissionsEntity;
import com.urbe.ms_auth.entity.RoleEntity;
import com.urbe.ms_auth.entity.UserEntity;
import com.urbe.ms_auth.enums.RoleNameEnum;
import com.urbe.ms_auth.repository.PermissionsRepository;
import com.urbe.ms_auth.repository.RoleRepository;
import com.urbe.ms_auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.init-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionsRepository permissionsRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisteredClientRepository registeredClientRepository;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           PermissionsRepository permissionsRepository, PasswordEncoder passwordEncoder,
                           RegisteredClientRepository registeredClientRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionsRepository = permissionsRepository;
        this.passwordEncoder = passwordEncoder;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public void run(String... args) {
        createRolesAndPermissions();
        createUsers();
        createOauth2Client();
    }

    private void createRolesAndPermissions() {
        if (roleRepository.count() > 0) {
            log.info("DataInitializer: Roles ya existen, no se insertan nuevos datos de roles.");
            return;
        }

        log.info("DataInitializer: Creando roles y permisos iniciales...");

        Arrays.stream(RoleNameEnum.values()).forEach(roleName -> {
            RoleEntity role = new RoleEntity();
            role.setRoleEnum(roleName);

            // Crear permisos específicos para este rol
            PermissionsEntity permissions = new PermissionsEntity();

            switch (roleName) {
                case ADMIN:
                    permissions.setCanRead(true);
                    permissions.setCanWrited(true);
                    permissions.setCanUpdate(true);
                    permissions.setCanDelete(true);
                    break;
                case USER:
                    permissions.setCanRead(true);
                    permissions.setCanWrited(true);
                    permissions.setCanUpdate(true);
                    permissions.setCanDelete(false);
                case OWNER:
                    permissions.setCanRead(true);
                    permissions.setCanWrited(true);
                    permissions.setCanUpdate(true);
                    permissions.setCanDelete(false);
                case AGENT:
                    permissions.setCanRead(true);
                    permissions.setCanWrited(true);
                    permissions.setCanUpdate(true);
                    permissions.setCanDelete(false);
                    break;
                case GUEST:
                default:
                    permissions.setCanRead(true);
                    permissions.setCanWrited(false);
                    permissions.setCanUpdate(false);
                    permissions.setCanDelete(false);
                    break;
            }

            // Importante: No guardamos los permisos explícitamente aquí.
            // Se guardarán en cascada cuando guardemos el rol.
            role.setPermissionsList(new HashSet<>(Set.of(permissions)));
            roleRepository.save(role);
            log.info("DataInitializer: Rol {} creado.", role.getRoleEnum());
        });
    }

    private void createUsers() {
        if (userRepository.count() > 0) {
            log.info("DataInitializer: Usuarios ya existen, no se insertan nuevos datos.");
            return;
        }

        log.info("DataInitializer: Creando usuarios de prueba...");

        // Usuario ADMIN
        createTestUser("admin@urbe.com", "admin123", RoleNameEnum.ADMIN);

        // Usuario USER
        createTestUser("user@urbe.com", "user123", RoleNameEnum.USER);

        // Usuario OWNER
        createTestUser("owner@urbe.com", "owner123", RoleNameEnum.OWNER);

        // Usuario GUEST
        createTestUser("guest@urbe.com", "guest123", RoleNameEnum.GUEST);
    }

    private void createTestUser(String email, String password, RoleNameEnum roleName) {
        RoleEntity role = roleRepository.findAll().stream()
                .filter(r -> r.getRoleEnum() == roleName)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error: Rol " + roleName + " no encontrado."));

        UserEntity user = new UserEntity();
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setAccountNoExpired(true);
        user.setAccountNoLocked(true);
        user.setCredentialNoExpired(true);
        user.setRoles(new HashSet<>(Set.of(role)));

        userRepository.save(user);
        log.info("DataInitializer: Usuario {} creado con rol {}", email, roleName);
    }

    private void createOauth2Client() {
        if (registeredClientRepository.findByClientId("gateway-client") != null) {
            log.info("DataInitializer: Cliente OAuth2 'gateway-client' ya existe.");
            return;
        }

        log.info("DataInitializer: Creando cliente OAuth2 'gateway-client'...");

        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("gateway-client")
                .clientSecret(passwordEncoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://127.0.0.1:8000/login/oauth2/code/gateway-client")
                .redirectUri("http://localhost:8000/login/oauth2/code/gateway-client")
                .postLogoutRedirectUri("http://127.0.0.1:8000/login")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("read")
                .scope("write")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(false) // CAMBIO: Desactivar PKCE temporalmente para depurar
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(1))
                        .build())
                .build();

        registeredClientRepository.save(oidcClient);
        log.info("DataInitializer: Cliente OAuth2 'gateway-client' creado exitosamente.");
    }
}
