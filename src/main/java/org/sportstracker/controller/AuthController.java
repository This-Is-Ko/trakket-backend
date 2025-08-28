package org.sportstracker.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.auth.LoginRequest;
import org.sportstracker.dto.auth.LoginResponse;
import org.sportstracker.dto.auth.ResendOtpRequest;
import org.sportstracker.dto.auth.UserInfoResponse;
import org.sportstracker.dto.auth.UserSignupOtpChallengeResponse;
import org.sportstracker.dto.auth.UserSignupResponse;
import org.sportstracker.dto.auth.UserSignupRequest;
import org.sportstracker.dto.auth.VerifyOtpRequest;
import org.sportstracker.service.auth.AuthService;
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