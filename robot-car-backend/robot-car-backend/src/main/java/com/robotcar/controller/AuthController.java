package com.robotcar.controller;

import com.robotcar.dto.auth.LoginRequest;
import com.robotcar.dto.auth.LoginResponse;
import com.robotcar.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request) {

        return authService.login(request);

    }

    @PostMapping("/register")
    public String register(
            @RequestBody com.robotcar.dto.auth.RegisterRequest request) {

        return authService.register(request);

    }
}