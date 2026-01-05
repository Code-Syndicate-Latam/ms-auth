package com.teamsoft.ms.auth.service;

import com.teamsoft.ms.auth.entities.Role;
import com.teamsoft.ms.auth.repository.RoleRepository;
import com.teamsoft.ms.auth.service.Impl.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setRoleId(1L);
        role.setRoleName("ADMIN");
        role.setPermissions(new ArrayList<>(List.of("users:read")));
    }

    @Test
    void findAllShouldReturnRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<Role> result = roleService.findAll();

        assertThat(result).hasSize(1);
        verify(roleRepository).findAll();
    }

    @Test
    void saveShouldInitializePermissionsWhenNull() {
        Role roleWithoutPermissions = new Role();
        roleWithoutPermissions.setRoleName("NEW");

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role saved = roleService.save(roleWithoutPermissions);

        assertThat(saved.getPermissions()).isNotNull();
        verify(roleRepository).save(roleWithoutPermissions);
    }

    @Test
    void deleteByIdShouldDelegateToRepository() {
        roleService.deleteById(10L);
        verify(roleRepository).deleteById(10L);
    }

    @Test
    void addPermissionShouldAppendWhenRoleExists() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role updated = roleService.addPermission(1L, "users:write");

        assertThat(updated.getPermissions()).contains("users:write");
        verify(roleRepository).findById(1L);
        verify(roleRepository).save(role);
    }

    @Test
    void addPermissionShouldThrowWhenRoleMissing() {
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> roleService.addPermission(2L, "x"));
        verify(roleRepository).findById(2L);
    }

    @Test
    void removePermissionShouldUpdateList() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role updated = roleService.removePermission(1L, "users:read");

        assertThat(updated.getPermissions()).doesNotContain("users:read");
        verify(roleRepository).save(role);
    }

    @Test
    void removePermissionShouldThrowWhenRoleMissing() {
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> roleService.removePermission(3L, "x"));
        verify(roleRepository).findById(3L);
    }
}

