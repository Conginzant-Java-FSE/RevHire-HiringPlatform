package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.UserLoginRequest;
import com.example.revhirehiringplatform.dto.request.UserRegistrationRequest;
import com.example.revhirehiringplatform.dto.response.AuthResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final com.example.revhirehiringplatform.repository.JobSeekerProfileRepository jobSeekerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse registerUser(UserRegistrationRequest registrationDto) {
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

        if (savedUser.getRole() == User.Role.JOB_SEEKER) {
            com.example.revhirehiringplatform.model.JobSeekerProfile profile = new com.example.revhirehiringplatform.model.JobSeekerProfile();
            profile.setUser(savedUser);
            profile.setLocation(registrationDto.getLocation());
            profile.setEmploymentStatus(registrationDto.getEmploymentStatus());
            jobSeekerProfileRepository.save(profile);
        }

        log.info("User registered successfully: {}", savedUser.getId());
        return AuthResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    public AuthResponse loginUser(UserLoginRequest loginDto) {
        log.info("Attempting login for email: {}", loginDto.getEmail());
        Optional<User> userOpt = userRepository.findByEmail(loginDto.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check if it's a BCrypt hash (usually starts with $2a$ or $2b$)
            if (user.getPassword() != null && user.getPassword().startsWith("$2")) {
                if (passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
                    log.info("Login successful for user (hashed): {}", user.getId());
                    return buildAuthResponse(user);
                }
            } else {
                // Fallback for legacy plain-text passwords
                if (loginDto.getPassword().equals(user.getPassword())) {
                    log.info("Legacy login successful for user: {}. Upgrading to secure hash.", user.getId());
                    // Upgrade to hash automatically
                    user.setPassword(passwordEncoder.encode(loginDto.getPassword()));
                    userRepository.save(user);
                    return buildAuthResponse(user);
                }
            }
        }
        log.warn("Login failed for email: {}", loginDto.getEmail());
        throw new RuntimeException("Invalid email or password");
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}