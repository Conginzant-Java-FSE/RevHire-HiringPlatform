package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.dto.response.JobSeekerProfileResponse;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.service.SavedResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employer/saved-resumes")
@RequiredArgsConstructor
@Slf4j
public class SavedResumeController {

    private final SavedResumeService savedResumeService;
    private final UserRepository userRepository;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        return userOpt.orElse(null);
    }

    @PostMapping("/{seekerId}")
    public ResponseEntity<?> saveResume(@PathVariable Long seekerId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized: Only employers can save resumes.");
        }
        try {
            savedResumeService.saveResume(seekerId, user);
            return ResponseEntity.ok("Resume saved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{seekerId}")
    public ResponseEntity<?> unsaveResume(@PathVariable Long seekerId,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized: Only employers can manage saved resumes.");
        }
        try {
            savedResumeService.unsaveResume(seekerId, user);
            return ResponseEntity.ok("Resume unsaved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getSavedResumes(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized: Only employers can view saved resumes.");
        }
        List<JobSeekerProfileResponse> savedResumes = savedResumeService.getSavedResumes(user);
        return ResponseEntity.ok(savedResumes);
    }
}
