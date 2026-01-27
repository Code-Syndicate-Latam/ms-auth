package com.urbe.ms_auth.entity;


import com.urbe.ms_auth.enums.RoleNameEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "Role", schema = "auth")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true )
    private Long  id;

    @Enumerated(EnumType.STRING)
    @Column(name = "RoleName", nullable = false, unique = true )
    private RoleNameEnum roleEnum;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "role_permissions", schema = "auth", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permissions_id"))
    private Set<PermissionsEntity> permissionsList = new HashSet<>();

    // Getters y Setters manuales
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleNameEnum getRoleEnum() {
        return roleEnum;
    }

    public void setRoleEnum(RoleNameEnum roleEnum) {
        this.roleEnum = roleEnum;
    }

    public Set<PermissionsEntity> getPermissionsList() {
        return permissionsList;
    }

    public void setPermissionsList(Set<PermissionsEntity> permissionsList) {
        this.permissionsList = permissionsList;
    }
}
