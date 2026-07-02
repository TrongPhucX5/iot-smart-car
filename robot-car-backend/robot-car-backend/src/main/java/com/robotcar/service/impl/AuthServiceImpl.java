package com.robotcar.service.impl;

import com.robotcar.dto.auth.LoginRequest;
import com.robotcar.dto.auth.LoginResponse;
import com.robotcar.dto.auth.RegisterRequest;
import com.robotcar.entity.User;
import com.robotcar.repository.UserRepository;
import com.robotcar.security.JwtService;
import com.robotcar.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl
        implements AuthService {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    @Override
    public LoginResponse login(
            LoginRequest request) {

        User user =
                userRepository
                        .findByUsername(
                                request.getUsername())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        // SỬA Ở ĐÂY
        if (!user.getPasswordHash()
                .equals(
                        request.getPassword())) {

            throw new RuntimeException(
                    "Invalid password");
        }

        String token =
                jwtService.generateToken(
                        user.getUsername());

        return new LoginResponse(
                user.getUserId(),
                user.getUsername(),
                user.getRole().name(),
                token
        );
    }

    @Override
    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPasswordHash(request.getPassword()); // Storing raw password to match existing logic
        newUser.setRole(User.Role.USER);
        newUser.setEnabled(true);

        userRepository.save(newUser);

        return "User registered successfully";
    }
}