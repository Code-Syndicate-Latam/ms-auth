package com.teamsoft.ms.auth.controller;


import com.teamsoft.ms.auth.model.request.LoginRequest;
import com.teamsoft.ms.auth.model.request.RegisterRequest;
import com.teamsoft.ms.auth.model.response.TokenResponse;
import com.teamsoft.ms.auth.service.Impl.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<TokenResponse> token(@RequestBody RegisterRequest req) {
        TokenResponse tokenResponse = authService.register(req);
        return ResponseEntity.ok(tokenResponse);
    }
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> token(@RequestBody LoginRequest req) {
        TokenResponse tokenResponse = authService.login(req);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader) {
        TokenResponse tokenResponse = authService.refresh(authHeader);
        return ResponseEntity.ok(tokenResponse);
    }

}
