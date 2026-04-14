package com.greenpulse.api.features.auth.service;

import com.greenpulse.api.features.auth.data.User;
import com.greenpulse.api.features.auth.data.UserRepository;
import com.greenpulse.api.features.auth.dto.AuthResponse;
import com.greenpulse.api.features.auth.dto.LoginRequest;
import com.greenpulse.api.features.auth.dto.SignupRequest;
import com.greenpulse.api.features.auth.dto.UserResponse;
import com.greenpulse.api.features.auth.exception.EmailAlreadyExistsException;
import com.greenpulse.api.features.auth.exception.UserNotFoundException;
import com.greenpulse.api.infrastructure.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtils jwtUtils;
        private final AuthenticationManager authenticationManager;
        private final LoginAttemptService loginAttemptService;

        public AuthResponse signup(SignupRequest request) {
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new EmailAlreadyExistsException(request.getEmail());
                }

                var user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .isVerified(true)
                                .build();
                userRepository.save(user);

                var jwtToken = jwtUtils.generateToken(user);
                return AuthResponse.builder()
                                .token(jwtToken)
                                .name(user.getName())
                                .email(user.getEmail())
                                .build();
        }

        public void verifyEmail(String token) {
                var user = userRepository.findByVerificationToken(token)
                                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

                if (user.getVerificationTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
                        throw new RuntimeException("Verification token expired");
                }

                user.setVerified(true);
                user.setVerificationToken(null);
                user.setVerificationTokenExpiry(null);
                userRepository.save(user);
        }

        public AuthResponse login(LoginRequest request, String clientIp) {
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(),
                                                        request.getPassword()));

                        var user = userRepository.findByEmail(request.getEmail())
                                        .orElseThrow();

                        loginAttemptService.loginSucceeded(clientIp);

                        var jwtToken = jwtUtils.generateToken(user);
                        return AuthResponse.builder()
                                        .token(jwtToken)
                                        .name(user.getName())
                                        .email(user.getEmail())
                                        .build();
                } catch (Exception e) {
                        loginAttemptService.loginFailed(clientIp);
                        throw e;
                }
        }

        public void forgotPassword(String email) {
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserNotFoundException(email));

                user.setPasswordResetToken(java.util.UUID.randomUUID().toString());
                user.setPasswordResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
                userRepository.save(user);

                System.out.println("Password Reset Token for " + email + ": " + user.getPasswordResetToken());
        }

        public void completePasswordReset(String token, String newPassword) {
                var user = userRepository.findByPasswordResetToken(token)
                                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

                if (user.getPasswordResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
                        throw new RuntimeException("Reset token expired");
                }

                user.setPassword(passwordEncoder.encode(newPassword));
                user.setPasswordResetToken(null);
                user.setPasswordResetTokenExpiry(null);
                userRepository.save(user);
        }

        public UserResponse getMe(String email) {
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserNotFoundException(email));
                return UserResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .build();
        }

        public AuthResponse updateProfile(String email, String newName) {
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserNotFoundException(email));
                user.setName(newName);
                userRepository.save(user);
                return AuthResponse.builder()
                                .name(user.getName())
                                .email(user.getEmail())
                                .build();
        }

        public void changePassword(String email, String oldPassword, String newPassword) {
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserNotFoundException(email));

                if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                        throw new RuntimeException("Invalid current password");
                }

                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
        }
}
