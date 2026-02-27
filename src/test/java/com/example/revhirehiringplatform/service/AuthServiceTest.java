package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.UserRegistrationRequest;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.example.revhirehiringplatform.repository.JobSeekerProfileRepository jobSeekerProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.example.revhirehiringplatform.service.AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_Success() {
        UserRegistrationRequest dto = new UserRegistrationRequest();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setRole(User.Role.JOB_SEEKER);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = authService.registerUser(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        UserRegistrationRequest dto = new UserRegistrationRequest();
        dto.setEmail("existing@example.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.registerUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    // loginUser tests removed as login is now handled via AuthenticationManager in
    // AuthController.
}
