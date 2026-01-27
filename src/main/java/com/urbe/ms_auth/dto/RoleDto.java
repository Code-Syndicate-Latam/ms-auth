package com.urbe.ms_auth.dto;

import com.urbe.ms_auth.enums.RoleNameEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto implements Serializable {
    private Long id;
    private RoleNameEnum roleEnum;
    private Set<PermissionsDto> permissionsList;
}