package com.urbe.ms_auth.dto;

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
public class CreateUserDto implements Serializable {
    private String username;
    private String password;
    private Set<Long> roleIds; // IDs de los roles a asignar
}