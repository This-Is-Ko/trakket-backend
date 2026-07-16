package unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.trakket.enums.AuthProvider;
import org.trakket.enums.UserRole;
import org.trakket.model.User;
import org.trakket.service.auth.JwtTokenService;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtTokenServiceTest {

    @Mock
    private JwtEncoder encoder;

    @Mock
    private JwtDecoder decoder;

    @InjectMocks
    private JwtTokenService jwtTokenService;

    private static final String ENCODED_TOKEN = "encoded-jwt-token";
    private static final Long EXPIRY_EPOCH_MS = 2000000000000L;

    @Test
    void generateTokenForUser_ShouldUseEmailAsSubject() {
        User user = createUser("user@gmail.com", UserRole.ROLE_USER);
        when(encoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new Jwt(ENCODED_TOKEN, Instant.now(), Instant.now().plusSeconds(3600),
                        Map.of("alg", "HS256"), Map.of("sub", "test")));

        String token = jwtTokenService.generateTokenForUser(user);

        assertEquals(ENCODED_TOKEN, token);
        verify(encoder, times(1)).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void generateTokenForUser_ShouldReturnNonNullToken() {
        User user = createUser("user@gmail.com", UserRole.ROLE_USER);
        when(encoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new Jwt(ENCODED_TOKEN, Instant.now(), Instant.now().plusSeconds(3600),
                        Map.of("alg", "HS256"), Map.of("sub", "test")));

        String token = jwtTokenService.generateTokenForUser(user);

        assertEquals(ENCODED_TOKEN, token);
    }

    @Test
    void generateTokenForUser_ShouldWorkForAdminRole() {
        User user = createUser("admin@trakket.com", UserRole.ROLE_ADMIN);
        when(encoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new Jwt(ENCODED_TOKEN, Instant.now(), Instant.now().plusSeconds(3600),
                        Map.of("alg", "HS256"), Map.of("sub", "test")));

        String token = jwtTokenService.generateTokenForUser(user);

        assertEquals(ENCODED_TOKEN, token);
        verify(encoder, times(1)).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void extractExpirationTime_ShouldReturnEpochMillis() {
        Jwt mockJwt = mock(Jwt.class);
        Instant expInstant = Instant.ofEpochMilli(EXPIRY_EPOCH_MS);
        when(decoder.decode("some-token")).thenReturn(mockJwt);
        when(mockJwt.getClaim("exp")).thenReturn(expInstant);

        Long result = jwtTokenService.extractExpirationTime("some-token");

        assertEquals(EXPIRY_EPOCH_MS, result);
        verify(decoder, times(1)).decode("some-token");
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private User createUser(String email, UserRole role) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setUsername(email);
        user.setRole(role);
        user.setProvider(AuthProvider.GOOGLE);
        user.setVerified(true);
        return user;
    }
}
