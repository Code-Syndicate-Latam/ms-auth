package com.teamsoft.ms.auth.config;

import com.teamsoft.ms.auth.entities.Permission;
import com.teamsoft.ms.auth.entities.PermissionRole;
import com.teamsoft.ms.auth.entities.Role;
import com.teamsoft.ms.auth.entities.keys.PermissionRoleId;
import com.teamsoft.ms.auth.repository.PermissionRepository;
import com.teamsoft.ms.auth.repository.PermissionRoleRepository;
import com.teamsoft.ms.auth.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.init-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionRoleRepository permissionRoleRepository;

    public DataInitializer(RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           PermissionRoleRepository permissionRoleRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionRoleRepository = permissionRoleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Evitar insertar datos si ya existen
        if (roleRepository.count() > 0 || permissionRepository.count() > 0 || permissionRoleRepository.count() > 0) {
            log.info("DataInitializer: tablas ya contienen datos; no se insertarán datos sintéticos.");
            return;
        }

        // Crear roles
        Role admin = new Role();
        admin.setRoleName("ADMIN");
        admin.setDescription("Administrator role");
        admin.setStatus(true);
        admin = roleRepository.save(admin);
        log.info("DataInitializer: role creado -> {}", admin);

        Role user = new Role();
        user.setRoleName("USER");
        user.setDescription("Regular user role");
        user.setStatus(true);
        user = roleRepository.save(user);
        log.info("DataInitializer: role creado -> {}", user);

        // Crear permisos
        Permission fullPerm = new Permission();
        fullPerm.setRead(true);
        fullPerm.setWrite(true);
        fullPerm.setUpdate(true);
        fullPerm.setDelete(true);
        fullPerm.setStatus(true);
        fullPerm = permissionRepository.save(fullPerm);
        log.info("DataInitializer: permission creado -> {}", fullPerm);

        Permission readOnly = new Permission();
        readOnly.setRead(true);
        readOnly.setWrite(false);
        readOnly.setUpdate(false);
        readOnly.setDelete(false);
        readOnly.setStatus(true);
        readOnly = permissionRepository.save(readOnly);
        log.info("DataInitializer: permission creado -> {}", readOnly);

        Permission writeOnly = new Permission();
        writeOnly.setRead(false);
        writeOnly.setWrite(true);
        writeOnly.setUpdate(false);
        writeOnly.setDelete(false);
        writeOnly.setStatus(true);
        writeOnly = permissionRepository.save(writeOnly);
        log.info("DataInitializer: permission creado -> {}", writeOnly);

        // Asociar permisos a roles
        PermissionRole pr1 = new PermissionRole();
        pr1.setId(new PermissionRoleId(admin.getRoleId(), fullPerm.getPermissionId()));
        pr1.setRole(admin);
        pr1.setPermission(fullPerm);
        permissionRoleRepository.save(pr1);
        log.info("DataInitializer: permission_role creado -> {}", pr1);

        PermissionRole pr2 = new PermissionRole();
        pr2.setId(new PermissionRoleId(user.getRoleId(), readOnly.getPermissionId()));
        pr2.setRole(user);
        pr2.setPermission(readOnly);
        permissionRoleRepository.save(pr2);
        log.info("DataInitializer: permission_role creado -> {}", pr2);

        PermissionRole pr3 = new PermissionRole();
        pr3.setId(new PermissionRoleId(admin.getRoleId(), writeOnly.getPermissionId()));
        pr3.setRole(admin);
        pr3.setPermission(writeOnly);
        permissionRoleRepository.save(pr3);
        log.info("DataInitializer: permission_role creado -> {}", pr3);

        log.info("DataInitializer: Inserción completada. Roles={}, Permissions={}, PermissionRoles={}",
                roleRepository.count(), permissionRepository.count(), permissionRoleRepository.count());
    }
}
