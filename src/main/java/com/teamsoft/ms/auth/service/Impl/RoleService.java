package com.teamsoft.ms.auth.service.Impl;

import com.teamsoft.ms.auth.entities.Role;
import com.teamsoft.ms.auth.repository.RoleRepository;
import com.teamsoft.ms.auth.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService  implements IRoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public Role save(Role role) {
        if (role.getPermissions() == null) {
            role.setPermissions(new ArrayList<>());
        }
        return roleRepository.save(role);
    }

    @Override
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    public Role addPermission(Long roleId, String permissionCode) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        role.getPermissions().add(permissionCode);
        return roleRepository.save(role);
    }
    @Override
    public Role removePermission(Long roleId, String permissionCode) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        role.getPermissions().remove(permissionCode);
        return roleRepository.save(role);
    }
}
