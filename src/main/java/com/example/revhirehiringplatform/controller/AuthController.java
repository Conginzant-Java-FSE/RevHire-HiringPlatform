package com.example.revhirehiringplatform.controller;


import com.example.revhirehiringplatform.dto.request.UserLoginRequest;
import com.example.revhirehiringplatform.dto.request.UserRegistrationRequest;
import com.example.revhirehiringplatform.dto.response.AuthResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationDto,
                                          HttpSession session) {
        try {
            AuthResponse response = authService.registerUser(registrationDto);

            User user = new User();
            user.setId(response.getId());
            user.setEmail(response.getEmail());
            user.setRole(response.getRole());
            user.setName(response.getName());
            session.setAttribute("user", user);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest loginDto, HttpSession session) {
        try {
            AuthResponse response = authService.loginUser(loginDto);
            User user = new User();
            user.setId(response.getId());
            user.setEmail(response.getEmail());
            user.setRole(response.getRole());
            user.setName(response.getName());
            session.setAttribute("user", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
}
