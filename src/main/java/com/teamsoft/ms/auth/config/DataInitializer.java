package com.teamsoft.ms.auth.config;

import com.teamsoft.ms.auth.entities.Role;
import com.teamsoft.ms.auth.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.init-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("DataInitializer: roles ya existen, no se insertan datos.");
            return;
        }

        Role admin = new Role();
        admin.setRoleName("ADMIN");
        admin.setDescription("Administrator role");
        admin.setStatus(true);
        admin.setPermissions(Arrays.asList(
                "users:read",
                "users:write",
                "users:update",
                "users:delete"
        ));
        roleRepository.save(admin);
        log.info("DataInitializer: role ADMIN creado con permisos {}", admin.getPermissions());

        Role user = new Role();
        user.setRoleName("USER");
        user.setDescription("Regular user role");
        user.setStatus(true);
        user.setPermissions(List.of("users:read"));
        roleRepository.save(user);
        log.info("DataInitializer: role USER creado con permisos {}", user.getPermissions());
    }
}
