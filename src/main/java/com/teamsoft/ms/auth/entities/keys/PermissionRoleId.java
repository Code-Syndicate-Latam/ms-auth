package com.teamsoft.ms.auth.entities.keys;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PermissionRoleId implements Serializable {

    private Long roleId;
    private Long permissionId;

}
