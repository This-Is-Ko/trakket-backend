package org.trakket.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.trakket.dto.auth.GoogleLoginRequest;
import org.trakket.dto.auth.LoginRequest;
import org.trakket.dto.auth.LoginResponse;
import org.trakket.dto.auth.UserInfoResponse;
import org.trakket.enums.AuthProvider;
import org.trakket.enums.UserRole;
import org.trakket.exception.GoogleAuthException;
import org.trakket.model.User;
import org.trakket.repository.UserRepository;
import org.trakket.security.AuthUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleTokenVerifier;

    public LoginResponse authenticate(LoginRequest authRequest, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);

        String jwtToken = jwtTokenService.generateToken(authentication);
        Long expiresAt = jwtTokenService.extractExpirationTime(jwtToken);

        setJwtCookie(response, jwtToken, expiresAt);

        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return new LoginResponse(authUser.getUser().getEmail(), expiresAt, authUser.getUser().getUsername());
    }

    @Transactional
    public LoginResponse authenticateWithGoogle(GoogleLoginRequest request, HttpServletResponse response) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.getIdToken());

        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String givenName = (String) payload.get("given_name");

        // Check if user already exists by Google ID (sub claim — stable across email changes)
        Optional<User> existingByGoogleId = userRepository.findByGoogleId(googleId);
        if (existingByGoogleId.isPresent()) {
            User user = existingByGoogleId.get();
            // Keep the email current — Google allows account email changes
            // Don't overwrite username — it may have been set from Google given name
            if (!user.getEmail().equals(email)) {
                user.setEmail(email);
                userRepository.save(user);
                log.atInfo().setMessage("Google login: updated email for user {} from {} to {}")
                        .addArgument(googleId)
                        .addArgument(user.getEmail())
                        .addArgument(email)
                        .log();
            }
            String jwtToken = jwtTokenService.generateTokenForUser(user);
            Long expiresAt = jwtTokenService.extractExpirationTime(jwtToken);
            setJwtCookie(response, jwtToken, expiresAt);
            log.atInfo().setMessage("Google login: existing Google user {}").addArgument(user.getEmail()).log();
            return new LoginResponse(user.getEmail(), expiresAt, user.getUsername());
        }

        // Check if email already registered (conflict with existing email/password user)
        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User existingUser = existingByEmail.get();
            if (existingUser.getProvider() == AuthProvider.EMAIL) {
                throw new GoogleAuthException(
                        "An account with this email already exists. Please log in with your email and password, "
                                + "then link your Google account in settings."
                );
            }
            // Provider is GOOGLE but different googleId — shouldn't happen normally, but handle it
            throw new GoogleAuthException("An account with this email already exists.");
        }

        // Create new user — use Google given name as username, fallback to email
        String username = (givenName != null && !givenName.isBlank()) ? givenName : email;
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setGoogleId(googleId);
        newUser.setProvider(AuthProvider.GOOGLE);
        newUser.setRole(UserRole.ROLE_USER);
        newUser.setVerified(true);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        userRepository.save(newUser);

        String jwtToken = jwtTokenService.generateTokenForUser(newUser);
        Long expiresAt = jwtTokenService.extractExpirationTime(jwtToken);
        setJwtCookie(response, jwtToken, expiresAt);

        log.atInfo().setMessage("Google login: new user created {}").addArgument(newUser.getEmail()).log();
        return new LoginResponse(newUser.getEmail(), expiresAt, newUser.getUsername());
    }

    public void logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("access_token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setAttribute("SameSite", "Strict");
        response.addCookie(jwtCookie);
    }

    public UserInfoResponse getUserInfo(Authentication authentication) {
        User user = (User) authentication.getDetails();

        return new UserInfoResponse(
                user.getEmail(),
                user.getUsername()
        );
    }

    private void setJwtCookie(HttpServletResponse response, String jwtToken, Long expiresAt) {
        Cookie jwtCookie = new Cookie("access_token", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) ((expiresAt - System.currentTimeMillis()) / 1000));
        jwtCookie.setAttribute("SameSite", "Strict");
        response.addCookie(jwtCookie);
    }
}
