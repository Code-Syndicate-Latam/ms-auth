package com.teamsoft.ms.auth.entities;


import com.teamsoft.ms.auth.entities.keys.PermissionRoleId;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "permission_role")
public class PermissionRole {

    @EmbeddedId
    private PermissionRoleId id;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;


}