package com.teamsoft.ms.auth.controller;

import com.teamsoft.ms.auth.entities.PermissionRole;
import com.teamsoft.ms.auth.service.Impl.PermissionRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permission-roles")
public class PermissionRoleController {

    @Autowired
    private PermissionRoleService permissionRoleService;

    @GetMapping
    public List<PermissionRole> getAllPermissionRoles() {
        return permissionRoleService.findAll();
    }

    @PostMapping("/assign")
    public ResponseEntity<PermissionRole> assignPermissionToRole(@RequestParam Long roleId, @RequestParam Long permissionId) {
        try {
            PermissionRole newAssignment = permissionRoleService.assignPermissionToRole(roleId, permissionId);
            return ResponseEntity.status(HttpStatus.CREATED).body(newAssignment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<Void> revokePermissionFromRole(@RequestParam Long roleId, @RequestParam Long permissionId) {
        try {
            permissionRoleService.revokePermissionFromRole(roleId, permissionId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}