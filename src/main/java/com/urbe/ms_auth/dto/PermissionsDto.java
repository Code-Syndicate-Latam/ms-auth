package com.urbe.ms_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionsDto implements Serializable {
    private Long id;
    private String canRead;
    private String canWrited;
    private String canUpdate;
    private String canDelete;
}