package com.teamsoft.ms.auth.service;

import com.teamsoft.ms.auth.model.request.RegisterRequest;

public interface IAuthService {
    void register(RegisterRequest req);
}
