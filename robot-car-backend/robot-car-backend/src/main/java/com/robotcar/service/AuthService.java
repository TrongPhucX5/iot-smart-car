package com.robotcar.service;

import com.robotcar.dto.auth.LoginRequest;
import com.robotcar.dto.auth.LoginResponse;
import com.robotcar.dto.auth.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    String register(RegisterRequest request);

}