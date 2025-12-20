package com.teamsoft.ms.auth.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserDto {
    private String userId;
    private String email;
    private String hashedPassword;
    private String role;
    private List<String> permissions;
}
