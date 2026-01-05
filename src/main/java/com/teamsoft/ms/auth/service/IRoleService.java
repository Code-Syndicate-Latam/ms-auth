package com.teamsoft.ms.auth.service;

import com.teamsoft.ms.auth.entities.Role;

import java.util.List;
import java.util.Optional;

public interface IRoleService {
    List<Role> findAll();
    Optional<Role> findById(Long id);
    Role save(Role role);
    void deleteById(Long id);
    Role removePermission(Long roleId, String permissionCode);
    Role addPermission(Long roleId, String permissionCode);
}
