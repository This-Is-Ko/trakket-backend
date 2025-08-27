package org.sportstracker.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.auth.LoginRequest;
import org.sportstracker.dto.auth.LoginResponse;
import org.sportstracker.dto.auth.ResendOtpRequest;
import org.sportstracker.dto.auth.UserSignupOtpChallengeResponse;
import org.sportstracker.dto.auth.UserSignupResponse;
import org.sportstracker.dto.auth.UserSignupRequest;
import org.sportstracker.dto.auth.VerifyOtpRequest;
import org.sportstracker.service.auth.AuthService;
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
    public UserSignupOtpChallengeResponse signup(@Valid @RequestBody UserSignupRequest userSignupRequest) {
        return authService.signup(userSignupRequest);
    }

    @PostMapping("/otp/resend")
    public UserSignupOtpChallengeResponse resendOtp(@RequestBody ResendOtpRequest resendOtpRequest) {
        return authService.resendOtp(resendOtpRequest);
    }

    @PostMapping("/otp/verify")
    public UserSignupResponse verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) {
        return authService.verifySignUpOtp(verifyOtpRequest);
    }

}