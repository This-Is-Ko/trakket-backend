package unit.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trakket.dto.auth.GoogleLoginRequest;
import org.trakket.dto.auth.LoginRequest;
import org.trakket.dto.auth.LoginResponse;
import org.trakket.enums.AuthProvider;
import org.trakket.enums.UserRole;
import org.trakket.exception.GoogleAuthException;
import org.trakket.model.User;
import org.trakket.repository.UserRepository;
import org.trakket.service.auth.AuthService;
import org.trakket.service.auth.GoogleTokenVerifier;
import org.trakket.service.auth.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private GoogleIdToken.Payload googlePayload;

    private LoginRequest loginRequest;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletResponse httpServletResponse;

    private final AtomicLong idGenerator = new AtomicLong(1);

    private static final String GOOGLE_ID = "google-sub-123";
    private static final String GOOGLE_EMAIL = "user@gmail.com";
    private static final String JWT_TOKEN = "test-jwt-token";
    private static final Long JWT_EXPIRY = 123456789L;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("testUser", "testPass");
    }

    // ─── email/password authenticate tests ──────────────────────────────────

    @Test
    void authenticate_ShouldReturnLoginResponseAndSetCookie_WhenCredentialsValid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(jwtTokenService.generateToken(authentication)).thenReturn("jwtToken123");
        when(jwtTokenService.extractExpirationTime("jwtToken123")).thenReturn(123456789L);

        LoginResponse response = authService.authenticate(loginRequest, httpServletResponse);

        assertNotNull(response);
        assertEquals("testUser", response.getName());
        assertEquals(123456789L, response.getExpiresAt());

        verify(httpServletResponse, times(1)).addCookie(argThat(cookie ->
                "access_token".equals(cookie.getName()) &&
                        "jwtToken123".equals(cookie.getValue()) &&
                        cookie.isHttpOnly() &&
                        cookie.getSecure() &&
                        "/".equals(cookie.getPath())
        ));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtTokenService, times(1)).generateToken(authentication);
        verify(jwtTokenService, times(1)).extractExpirationTime("jwtToken123");
    }

    // ─── Google OAuth: authenticateWithGoogle tests ─────────────────────────

    @Test
    void googleLogin_ShouldCreateNewUserAndReturnResponse() {
        // Arrange: no existing user
        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token");
        when(googleTokenVerifier.verify("google-id-token")).thenReturn(googlePayload);
        when(googlePayload.getSubject()).thenReturn(GOOGLE_ID);
        when(googlePayload.getEmail()).thenReturn(GOOGLE_EMAIL);
        when(userRepository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(GOOGLE_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded-random-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(idGenerator.getAndIncrement());
            return u;
        });
        when(jwtTokenService.generateTokenForUser(any(User.class))).thenReturn(JWT_TOKEN);
        when(jwtTokenService.extractExpirationTime(JWT_TOKEN)).thenReturn(JWT_EXPIRY);

        // Act
        LoginResponse response = authService.authenticateWithGoogle(request, httpServletResponse);

        // Assert: response
        assertNotNull(response);
        assertEquals(GOOGLE_EMAIL, response.getName());
        assertEquals(JWT_EXPIRY, response.getExpiresAt());

        // Assert: user saved with correct fields
        verify(userRepository, times(1)).save(argThat(user ->
                GOOGLE_EMAIL.equals(user.getEmail()) &&
                        GOOGLE_EMAIL.equals(user.getUsername()) &&
                        GOOGLE_ID.equals(user.getGoogleId()) &&
                        user.getProvider() == AuthProvider.GOOGLE &&
                        user.getRole() == UserRole.ROLE_USER &&
                        user.isVerified()
        ));

        // Assert: cookie set
        verify(httpServletResponse, times(1)).addCookie(argThat(cookie ->
                "access_token".equals(cookie.getName()) &&
                        JWT_TOKEN.equals(cookie.getValue()) &&
                        cookie.isHttpOnly() &&
                        cookie.getSecure() &&
                        "/".equals(cookie.getPath())
        ));

        verify(jwtTokenService, times(1)).generateTokenForUser(any(User.class));
        verify(jwtTokenService, times(1)).extractExpirationTime(JWT_TOKEN);
    }

    @Test
    void googleLogin_ShouldReturnExistingUser_WhenGoogleIdMatches() {
        // Arrange: user exists by googleId, same email
        User existingUser = createGoogleUser(1L, GOOGLE_ID, GOOGLE_EMAIL);

        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token");
        when(googleTokenVerifier.verify("google-id-token")).thenReturn(googlePayload);
        when(googlePayload.getSubject()).thenReturn(GOOGLE_ID);
        when(googlePayload.getEmail()).thenReturn(GOOGLE_EMAIL);
        when(userRepository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.of(existingUser));
        when(jwtTokenService.generateTokenForUser(existingUser)).thenReturn(JWT_TOKEN);
        when(jwtTokenService.extractExpirationTime(JWT_TOKEN)).thenReturn(JWT_EXPIRY);

        // Act
        LoginResponse response = authService.authenticateWithGoogle(request, httpServletResponse);

        // Assert
        assertNotNull(response);
        assertEquals(GOOGLE_EMAIL, response.getName());

        // Should not save (email hasn't changed)
        verify(userRepository, never()).save(any(User.class));

        verify(httpServletResponse, times(1)).addCookie(any());
        verify(jwtTokenService, times(1)).generateTokenForUser(existingUser);
    }

    @Test
    void googleLogin_ShouldUpdateEmail_WhenGoogleUserChangedEmail() {
        // Arrange: user exists by googleId, but with a different (old) email
        User existingUser = createGoogleUser(1L, GOOGLE_ID, "old@gmail.com");
        String newEmail = "new@gmail.com";

        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token");
        when(googleTokenVerifier.verify("google-id-token")).thenReturn(googlePayload);
        when(googlePayload.getSubject()).thenReturn(GOOGLE_ID);
        when(googlePayload.getEmail()).thenReturn(newEmail);
        when(userRepository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.of(existingUser));
        when(jwtTokenService.generateTokenForUser(existingUser)).thenReturn(JWT_TOKEN);
        when(jwtTokenService.extractExpirationTime(JWT_TOKEN)).thenReturn(JWT_EXPIRY);

        // Act
        LoginResponse response = authService.authenticateWithGoogle(request, httpServletResponse);

        // Assert
        assertNotNull(response);
        assertEquals(newEmail, response.getName());

        // Email and username should be updated
        assertEquals(newEmail, existingUser.getEmail());
        assertEquals(newEmail, existingUser.getUsername());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void googleLogin_ShouldThrow_WhenEmailExistsWithEmailProvider() {
        // Arrange: email registered via email/password
        User emailUser = new User();
        emailUser.setId(1L);
        emailUser.setEmail(GOOGLE_EMAIL);
        emailUser.setProvider(AuthProvider.EMAIL);
        emailUser.setRole(UserRole.ROLE_USER);

        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token");
        when(googleTokenVerifier.verify("google-id-token")).thenReturn(googlePayload);
        when(googlePayload.getSubject()).thenReturn(GOOGLE_ID);
        when(googlePayload.getEmail()).thenReturn(GOOGLE_EMAIL);
        when(userRepository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(GOOGLE_EMAIL)).thenReturn(Optional.of(emailUser));

        // Act & Assert
        GoogleAuthException exception = assertThrows(GoogleAuthException.class,
                () -> authService.authenticateWithGoogle(request, httpServletResponse));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenService, never()).generateTokenForUser(any());
    }

    @Test
    void googleLogin_ShouldThrow_WhenEmailExistsWithDifferentGoogleProvider() {
        // Arrange: email exists but with a different googleId
        User googleUser = new User();
        googleUser.setId(1L);
        googleUser.setEmail(GOOGLE_EMAIL);
        googleUser.setGoogleId("different-google-id");
        googleUser.setProvider(AuthProvider.GOOGLE);
        googleUser.setRole(UserRole.ROLE_USER);

        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token");
        when(googleTokenVerifier.verify("google-id-token")).thenReturn(googlePayload);
        when(googlePayload.getSubject()).thenReturn(GOOGLE_ID);
        when(googlePayload.getEmail()).thenReturn(GOOGLE_EMAIL);
        when(userRepository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(GOOGLE_EMAIL)).thenReturn(Optional.of(googleUser));

        // Act & Assert
        GoogleAuthException exception = assertThrows(GoogleAuthException.class,
                () -> authService.authenticateWithGoogle(request, httpServletResponse));

        assertTrue(exception.getMessage().contains("already exists"));
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private User createGoogleUser(Long id, String googleId, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(email);
        user.setGoogleId(googleId);
        user.setProvider(AuthProvider.GOOGLE);
        user.setRole(UserRole.ROLE_USER);
        user.setVerified(true);
        return user;
    }
}
