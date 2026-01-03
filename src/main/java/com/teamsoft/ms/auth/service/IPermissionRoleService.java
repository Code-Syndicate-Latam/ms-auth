package com.teamsoft.ms.auth.service;

import com.teamsoft.ms.auth.entities.PermissionRole;
import com.teamsoft.ms.auth.entities.keys.PermissionRoleId;

import java.util.List;
import java.util.Optional;

public interface IPermissionRoleService {
    List<PermissionRole> findAll();
    Optional<PermissionRole> findById(PermissionRoleId id);
    PermissionRole save(PermissionRole permissionRole);
    void deleteById(PermissionRoleId id);
    PermissionRole assignPermissionToRole(Long roleId, Long permissionId);
    void revokePermissionFromRole(Long roleId, Long permissionId);
}
