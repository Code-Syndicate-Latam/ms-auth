package com.teamsoft.ms.auth.controller;

import com.teamsoft.ms.auth.entities.Permission;
import com.teamsoft.ms.auth.service.Impl.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping
    public List<Permission> getAllPermissions() {
        return permissionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermissionById(@PathVariable Long id) {
        Optional<Permission> permission = permissionService.findById(id);
        return permission.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        Permission savedPermission = permissionService.save(permission);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPermission);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Permission> updatePermission(@PathVariable Long id, @RequestBody Permission permissionDetails) {
        Optional<Permission> optionalPermission = permissionService.findById(id);
        if (optionalPermission.isPresent()) {
            Permission permission = optionalPermission.get();
            permission.setRead(permissionDetails.isRead());
            permission.setWrite(permissionDetails.isWrite());
            permission.setUpdate(permissionDetails.isUpdate());
            permission.setDelete(permissionDetails.isDelete());
            permission.setStatus(permissionDetails.isStatus());
            return ResponseEntity.ok(permissionService.save(permission));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        if (permissionService.findById(id).isPresent()) {
            permissionService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}