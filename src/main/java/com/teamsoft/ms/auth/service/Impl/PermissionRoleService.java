package com.teamsoft.ms.auth.service.Impl;

import com.teamsoft.ms.auth.entities.Permission;
import com.teamsoft.ms.auth.entities.PermissionRole;
import com.teamsoft.ms.auth.entities.Role;
import com.teamsoft.ms.auth.entities.keys.PermissionRoleId;
import com.teamsoft.ms.auth.repository.PermissionRepository;
import com.teamsoft.ms.auth.repository.PermissionRoleRepository;
import com.teamsoft.ms.auth.repository.RoleRepository;
import com.teamsoft.ms.auth.service.IPermissionRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionRoleService implements IPermissionRoleService {

    @Autowired
    private PermissionRoleRepository permissionRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public List<PermissionRole> findAll() {
        return permissionRoleRepository.findAll();
    }

    @Override
    public Optional<PermissionRole> findById(PermissionRoleId id) {
        return permissionRoleRepository.findById(id);
    }

    @Override
    public PermissionRole save(PermissionRole permissionRole) {
        return permissionRoleRepository.save(permissionRole);
    }

    @Override
    public void deleteById(PermissionRoleId id) {
        permissionRoleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PermissionRole assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con id: " + permissionId));

        PermissionRoleId permissionRoleId = new PermissionRoleId(roleId, permissionId);
        PermissionRole newAssignment = new PermissionRole();
        newAssignment.setId(permissionRoleId);
        newAssignment.setRole(role);
        newAssignment.setPermission(permission);

        return permissionRoleRepository.save(newAssignment);
    }

    @Override
    @Transactional
    public void revokePermissionFromRole(Long roleId, Long permissionId) {
        PermissionRoleId permissionRoleId = new PermissionRoleId(roleId, permissionId);

        if (!permissionRoleRepository.existsById(permissionRoleId)) {
            throw new RuntimeException("La asignación entre el rol " + roleId + " y el permiso " + permissionId + " no existe.");
        }

        permissionRoleRepository.deleteById(permissionRoleId);
    }
}