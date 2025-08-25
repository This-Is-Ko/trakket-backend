package org.sportstracker.service;

import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.LoginRequest;
import org.sportstracker.dto.LoginResponse;
import org.sportstracker.dto.UserSignupRequest;
import org.sportstracker.dto.UserSignupResponse;
import org.sportstracker.enums.UserRole;
import org.sportstracker.exception.UserAlreadyExistsException;
import org.sportstracker.model.User;
import org.sportstracker.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse authenticate(LoginRequest authRequest) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);

        String jwtToken = jwtTokenService.generateToken(authentication);
        Long expiresAt = jwtTokenService.extractExpirationTime(jwtToken);

        return new LoginResponse(jwtToken, authentication.getName(), expiresAt);
    }

    public UserSignupResponse signup(UserSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already taken");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);

        return new UserSignupResponse(user.getId(), user.getUsername());
    }
}
