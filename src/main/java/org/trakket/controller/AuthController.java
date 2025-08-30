package org.trakket.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.trakket.dto.auth.LoginRequest;
import org.trakket.dto.auth.LoginResponse;
import org.trakket.dto.auth.ResendOtpRequest;
import org.trakket.dto.auth.UserInfoResponse;
import org.trakket.dto.auth.UserSignupOtpChallengeResponse;
import org.trakket.dto.auth.UserSignupResponse;
import org.trakket.dto.auth.UserSignupRequest;
import org.trakket.dto.auth.VerifyOtpRequest;
import org.trakket.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest authRequest, HttpServletResponse response) {
        return authService.authenticate(authRequest, response);
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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserInfoResponse userInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return authService.getUserInfo(authentication);
    }

}