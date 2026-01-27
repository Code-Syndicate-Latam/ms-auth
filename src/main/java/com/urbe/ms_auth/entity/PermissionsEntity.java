package com.urbe.ms_auth.entity;


import com.urbe.ms_auth.enums.RoleNameEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "Permissions", schema = "auth")
public class PermissionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true )
    private Long  id;

    @Column(name = "canRead", nullable = false, columnDefinition = "boolean default true")
    private Boolean canRead;

    @Column(name = "canWrited", nullable = false, columnDefinition = "boolean default false")
    private Boolean canWrited;

    @Column(name = "canUpdate", nullable = false, columnDefinition = "boolean default false")
    private Boolean canUpdate;

    @Column(name = "canDelete", nullable = false, columnDefinition = "boolean default false")
    private Boolean canDelete;

    // Getters y Setters manuales
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCanRead() {
        return canRead;
    }

    public void setCanRead(Boolean canRead) {
        this.canRead = canRead;
    }

    public Boolean getCanWrited() {
        return canWrited;
    }

    public void setCanWrited(Boolean canWrited) {
        this.canWrited = canWrited;
    }

    public Boolean getCanUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(Boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }
}
