package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.UserRegistrationRequest;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final com.example.revhirehiringplatform.repository.JobSeekerProfileRepository jobSeekerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final com.example.revhirehiringplatform.repository.PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public void initiatePasswordReset(String email) {
        log.info("Initiating password reset for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Delete existing tokens if any
        passwordResetTokenRepository.deleteByUser(user);

        String token = java.util.UUID.randomUUID().toString();
        com.example.revhirehiringplatform.model.PasswordResetToken resetToken = new com.example.revhirehiringplatform.model.PasswordResetToken(token, user, 30); // 30
                                                                                                                     // mins
                                                                                                                     // expiry
        passwordResetTokenRepository.save(resetToken);

        // Simulate sending email
        log.info("Password reset token generated for {}: {}", email, token);
        // In a real app, send email here.
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");
        com.example.revhirehiringplatform.model.PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        if (resetToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
        log.info("Password reset successfully for user: {}", user.getEmail());

        auditLogService.logAction("User", user.getId(), "PASSWORD_RESET", null, "Password reset via token", user);
    }

    @Transactional
    public void updatePassword(User user, String oldPassword, String newPassword) {
        log.info("Updating password for user: {}", user.getEmail());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated successfully for user: {}", user.getEmail());

        auditLogService.logAction("User", user.getId(), "PASSWORD_UPDATED", null, "Password updated manually", user);
    }

    public User registerUser(UserRegistrationRequest registrationDto) {
        log.info("Attempting to register user with email: {}", registrationDto.getEmail());
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            log.warn("Registration failed. Email already exists: {}", registrationDto.getEmail());
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        user.setPhone(registrationDto.getPhone());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(registrationDto.getRole());

        User savedUser = userRepository.save(user);

        // If Job Seeker, create initial profile
        if (savedUser.getRole() == User.Role.JOB_SEEKER) {
            com.example.revhirehiringplatform.model.JobSeekerProfile profile = new com.example.revhirehiringplatform.model.JobSeekerProfile();
            profile.setUser(savedUser);
            profile.setLocation(registrationDto.getLocation());
            profile.setEmploymentStatus(registrationDto.getEmploymentStatus());
            jobSeekerProfileRepository.save(profile);
        }

        // Audit logging
        auditLogService.logAction(
                "User",
                savedUser.getId(),
                "USER_REGISTERED",
                null,
                "Role: " + savedUser.getRole().name(),
                savedUser);

        log.info("User registered successfully: {}", savedUser.getId());
        return savedUser;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
