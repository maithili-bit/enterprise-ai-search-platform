package com.stackera.auth.service;

import com.stackera.auth.dto.AuthResponse;
import com.stackera.auth.dto.LoginRequest;
import com.stackera.auth.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);


}
