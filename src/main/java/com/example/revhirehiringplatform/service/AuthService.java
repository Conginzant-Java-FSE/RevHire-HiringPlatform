package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.UserRegistrationRequest;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final com.example.revhirehiringplatform.repository.JobSeekerProfileRepository jobSeekerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

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


        if (savedUser.getRole() == User.Role.JOB_SEEKER) {
            com.example.revhirehiringplatform.model.JobSeekerProfile profile = new com.example.revhirehiringplatform.model.JobSeekerProfile();
            profile.setUser(savedUser);
            profile.setLocation(registrationDto.getLocation());
            profile.setEmploymentStatus(registrationDto.getEmploymentStatus());
            jobSeekerProfileRepository.save(profile);
        }


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
}
