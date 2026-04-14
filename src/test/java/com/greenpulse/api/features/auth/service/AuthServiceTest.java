package com.greenpulse.api.features.auth.service;

import com.greenpulse.api.features.auth.data.User;
import com.greenpulse.api.features.auth.data.UserRepository;
import com.greenpulse.api.features.auth.dto.AuthResponse;
import com.greenpulse.api.features.auth.dto.LoginRequest;
import com.greenpulse.api.features.auth.dto.SignupRequest;
import com.greenpulse.api.infrastructure.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signup_ShouldGenerateVerificationToken() {
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        AuthResponse response = authService.signup(request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_ShouldFailIfUnverified() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        User user = User.builder().email("test@example.com").isVerified(false).build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.login(request, "127.0.0.1"));
    }

    @Test
    void verifyEmail_ShouldSetVerifiedTrue() {
        String token = "valid_token";
        User user = User.builder().email("test@example.com").isVerified(false)
                .verificationToken(token)
                .verificationTokenExpiry(java.time.LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));

        authService.verifyEmail(token);

        assertTrue(user.isVerified());
        assertNull(user.getVerificationToken());
    }

    @Test
    void forgotPassword_ShouldGenerateResetToken() {
        String email = "test@example.com";
        User user = User.builder().email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        authService.forgotPassword(email);

        assertNotNull(user.getPasswordResetToken());
        assertNotNull(user.getPasswordResetTokenExpiry());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void completePasswordReset_ShouldUpdatePassword() {
        String token = "reset_token";
        User user = User.builder().email("test@example.com")
                .passwordResetToken(token)
                .passwordResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new_password")).thenReturn("hashed_new_password");

        authService.completePasswordReset(token, "new_password");

        assertEquals("hashed_new_password", user.getPassword());
        assertNull(user.getPasswordResetToken());
    }

    @Test
    void login_ShouldSucceedAndResetAttempts() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        User user = User.builder().email("test@example.com").isVerified(true).build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(any())).thenReturn("jwt_token");

        authService.login(request, "127.0.0.1");

        verify(loginAttemptService, times(1)).loginSucceeded("127.0.0.1");
    }
}
