package com.teamsoft.ms.auth.service;

import com.teamsoft.ms.auth.model.dto.UserDto;
import com.teamsoft.ms.auth.model.request.LoginRequest;
import com.teamsoft.ms.auth.model.request.RegisterRequest;
import com.teamsoft.ms.auth.model.response.TokenResponse;

public interface IAuthService {
    TokenResponse register(RegisterRequest req);
    TokenResponse login(LoginRequest req);
    TokenResponse refresh(String authHeader);
}
