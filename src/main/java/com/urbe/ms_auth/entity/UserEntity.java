package com.urbe.ms_auth.entity;

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
@Table(name = "Users", schema = "auth")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true )
    private Long  id;

    @Column(name = "Email", nullable = false, unique = true, length = 300)
    private String username;

    @Column(name = "Password", nullable = false,length = 300)
    private String password;

    @Column(name = "is_Enabled", nullable = false, columnDefinition = "boolean default true")
    private boolean isEnabled;

    // se utiliza (SET) ya que no permite valores repetidos al momento de la creación de los roles
    // Cambiado CascadeType.ALL a CascadeType.MERGE para evitar errores con roles existentes
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "user_role", schema = "auth", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    @Column(name = "account_No_Expired", nullable = false, columnDefinition = "boolean default true")
    private boolean accountNoExpired;

    @Column(name = "account_No_Locked", nullable = false, columnDefinition = "boolean default true")
    private boolean accountNoLocked;

    @Column(name = "credential_No_Expired", nullable = false, columnDefinition = "boolean default true")
    private boolean credentialNoExpired;

    // Getters y Setters manuales
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    public boolean isAccountNoExpired() {
        return accountNoExpired;
    }

    public void setAccountNoExpired(boolean accountNoExpired) {
        this.accountNoExpired = accountNoExpired;
    }

    public boolean isAccountNoLocked() {
        return accountNoLocked;
    }

    public void setAccountNoLocked(boolean accountNoLocked) {
        this.accountNoLocked = accountNoLocked;
    }

    public boolean isCredentialNoExpired() {
        return credentialNoExpired;
    }

    public void setCredentialNoExpired(boolean credentialNoExpired) {
        this.credentialNoExpired = credentialNoExpired;
    }
}
