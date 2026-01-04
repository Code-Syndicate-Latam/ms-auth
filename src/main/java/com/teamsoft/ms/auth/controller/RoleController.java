package com.teamsoft.ms.auth.controller;

import com.teamsoft.ms.auth.entities.Role;
import com.teamsoft.ms.auth.service.Impl.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {


    private final RoleService roleService;

    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        Optional<Role> role = roleService.findById(id);
        return role.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role savedRole = roleService.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRole);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role roleDetails) {
        Optional<Role> optionalRole = roleService.findById(id);
        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();
            role.setRoleName(roleDetails.getRoleName());
            role.setDescription(roleDetails.getDescription());
            role.setStatus(roleDetails.isStatus());
            role.setPermissions(roleDetails.getPermissions());
            return ResponseEntity.ok(roleService.save(role));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/permissions")
    public ResponseEntity<Role> addPermission(@PathVariable Long id, @RequestParam String permissionCode) {
        if (!isValidPermission(permissionCode)) {
            return ResponseEntity.badRequest().build();
        }
        Role updated = roleService.addPermission(id, permissionCode);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/permissions")
    public ResponseEntity<Role> removePermission(@PathVariable Long id, @RequestParam String permissionCode) {
        if (!isValidPermission(permissionCode)) {
            return ResponseEntity.badRequest().build();
        }
        Role updated = roleService.removePermission(id, permissionCode);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        if (roleService.findById(id).isPresent()) {
            roleService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isValidPermission(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return false;
        }
        return permissionCode.contains(":");
    }
}
