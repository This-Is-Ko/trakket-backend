package unit;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sportstracker.dto.auth.LoginRequest;
import org.sportstracker.dto.auth.LoginResponse;
import org.sportstracker.dto.auth.UserSignupOtpChallengeResponse;
import org.sportstracker.dto.auth.UserSignupRequest;
import org.sportstracker.model.User;
import org.sportstracker.repository.UserRepository;
import org.sportstracker.service.auth.AuthService;
import org.sportstracker.service.auth.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
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

    private LoginRequest loginRequest;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletResponse httpServletResponse;

    private UserSignupRequest signupRequest;

    private final AtomicLong idGenerator = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("testUser", "testPass");
        signupRequest = new UserSignupRequest("newUser","newuse@gmail.com", "newPass");
    }

    @Test
    void authenticate_ShouldReturnLoginResponseAndSetCookie_WhenCredentialsValid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(jwtTokenService.generateToken(authentication)).thenReturn("jwtToken123");
        when(jwtTokenService.extractExpirationTime("jwtToken123")).thenReturn(123456789L);

        // Act
        LoginResponse response = authService.authenticate(loginRequest, httpServletResponse);

        // Assert body
        assertNotNull(response);
        assertEquals("testUser", response.getName());
        assertEquals(123456789L, response.getExpiresAt());

        // Assert cookie added
        verify(httpServletResponse, times(1)).addCookie(argThat(cookie ->
                "access_token".equals(cookie.getName()) &&
                        "jwtToken123".equals(cookie.getValue()) &&
                        cookie.isHttpOnly() &&
                        cookie.getSecure() &&
                        "/".equals(cookie.getPath())
        ));

        // Verify calls
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtTokenService, times(1)).generateToken(authentication);
        verify(jwtTokenService, times(1)).extractExpirationTime("jwtToken123");
    }

    @Test
    void signup_ShouldCreateUser_WhenUsernameNotTaken() {
        // Arrange
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(idGenerator.getAndIncrement()); // simulate DB assigning ID
            return u;
        });

        // Act
        UserSignupOtpChallengeResponse response = authService.signup(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals("newUser", response.getUsername());

        verify(userRepository, times(1)).existsByUsername("newUser");
        verify(passwordEncoder, times(1)).encode("newPass");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("newUser")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.signup(signupRequest));

        assertEquals("Username already taken", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername("newUser");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}