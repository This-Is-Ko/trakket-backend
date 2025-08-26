package org.sportstracker.service;

import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.LoginRequest;
import org.sportstracker.dto.LoginResponse;
import org.sportstracker.dto.ResendOtpRequest;
import org.sportstracker.dto.UserSignupOtpChallengeResponse;
import org.sportstracker.dto.UserSignupRequest;
import org.sportstracker.dto.UserSignupResponse;
import org.sportstracker.dto.VerifyOtpRequest;
import org.sportstracker.enums.UserRole;
import org.sportstracker.exception.OtpException;
import org.sportstracker.exception.SignupException;
import org.sportstracker.exception.UserAlreadyExistsException;
import org.sportstracker.model.User;
import org.sportstracker.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
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

    @Transactional
    public UserSignupOtpChallengeResponse signup(UserSignupRequest request) {
        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.isVerified()) {
                throw new UserAlreadyExistsException("Email already taken.");
            } else {
                otpService.prepareAndSendSignUpOtp(existingUser);
                return new UserSignupOtpChallengeResponse(existingUser.getUsername());
            }
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        user.setVerified(false);
        userRepository.save(user);

        otpService.prepareAndSendSignUpOtp(user);

        return new UserSignupOtpChallengeResponse(user.getUsername());
    }

    public UserSignupOtpChallengeResponse resendOtp(ResendOtpRequest resendOtpRequest) {
        Optional<User> userOpt = userRepository.findByEmail(resendOtpRequest.getEmail());
        if (userOpt.isEmpty()) {
            throw new SignupException("User not found.");
        }
        User user = userOpt.get();
        if (user.isVerified()) {
            throw new SignupException("User already verified.");
        }
        otpService.prepareAndSendSignUpOtp(user);
        return new UserSignupOtpChallengeResponse(user.getUsername());
    }

    @Transactional
    public UserSignupResponse verifySignUpOtp(VerifyOtpRequest verifyOtpRequest) {
        Optional<User> userObject = userRepository.findByEmail(verifyOtpRequest.getEmail());
        if (userObject.isEmpty()) {
            throw new SignupException("User not found.");
        }
        User user = userObject.get();
        if (user.isVerified()) {
            throw new SignupException("User already verified.");
        }
        otpService.validateOtp(user, verifyOtpRequest.getOtp());

        user.setVerified(true);
        userRepository.save(user);
        return new UserSignupResponse(user.getUsername());
    }
}
