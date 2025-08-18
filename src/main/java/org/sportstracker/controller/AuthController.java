package org.sportstracker.controller;

import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.LoginRequest;
import org.sportstracker.dto.LoginResponse;
import org.sportstracker.dto.UserSignupResponse;
import org.sportstracker.dto.UserSignupRequest;
import org.sportstracker.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest authRequest) {
        return authService.authenticate(authRequest);
    }

    @PostMapping("/signup")
    public UserSignupResponse signup(@RequestBody UserSignupRequest userSignupRequest) {
        return authService.signup(userSignupRequest);
    }
}