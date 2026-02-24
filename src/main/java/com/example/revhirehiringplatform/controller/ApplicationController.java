package com.example.revhirehiringplatform.controller;



import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.dto.request.ApplicationNoteRequest;
import com.example.revhirehiringplatform.dto.response.ApplicationNoteResponse;
import com.example.revhirehiringplatform.model.Application;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;
    private final com.example.revhirehiringplatform.service.ApplicationWithdrawalService withdrawalService;
    private final com.example.revhirehiringplatform.service.ApplicationUpdateService updateService;
    private final UserRepository userRepository;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        return userOpt.orElse(null);
    }

    @PostMapping("/apply/{jobId}")
    public ResponseEntity<?> applyForJob(@PathVariable("jobId") Long jobId,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ApplicationResponse application = applicationService.applyForJob(jobId, user);
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<ApplicationResponse> applications = applicationService.getMyApplications(user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getApplicationsForJob(@PathVariable("jobId") Long jobId,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<ApplicationResponse> applications = applicationService.getApplicationsForJob(jobId, user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("applicationId") Long applicationId,
                                          @RequestParam("status") Application.ApplicationStatus status,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ApplicationResponse application = applicationService.updateApplicationStatus(applicationId, status, user);
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{applicationId}/withdraw")
    public ResponseEntity<?> withdrawApplication(@PathVariable("applicationId") Long applicationId,
                                                 @RequestParam(required = false, name = "reason") String reason,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ApplicationResponse application = withdrawalService.withdrawApplication(applicationId, reason, user);
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{applicationId}/notes")
    public ResponseEntity<?> addNoteToApplication(@PathVariable("applicationId") Long applicationId,
                                                  @RequestBody ApplicationNoteRequest note, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ApplicationNoteResponse applicationNote = updateService.addNoteToApplication(applicationId,
                    note.getNoteText(), user);
            return ResponseEntity.ok(applicationNote);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/bulk-status")
    public ResponseEntity<?> updateBulkStatus(
            @RequestBody java.util.Map<String, Object> request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<?> applicationIdsRaw = (List<?>) request.get("applicationIds");
            List<Long> applicationIds = applicationIdsRaw.stream()
                    .filter(Number.class::isInstance)
                    .map(n -> ((Number) n).longValue())
                    .toList();
            String statusStr = (String) request.get("status");
            Application.ApplicationStatus status = Application.ApplicationStatus.valueOf(statusStr.toUpperCase());

            List<ApplicationResponse> applications = applicationService.updateBulkStatus(applicationIds, status, user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/job/{jobId}/search")
    public ResponseEntity<?> searchApplicantsForJob(@PathVariable("jobId") Long jobId,
                                                    @RequestParam(required = false, name = "name") String name,
                                                    @RequestParam(required = false, name = "skill") String skill,
                                                    @RequestParam(required = false, name = "experience") String experience,
                                                    @RequestParam(required = false, name = "education") String education,
                                                    @RequestParam(required = false, name = "appliedAfter") String appliedAfter,
                                                    @RequestParam(required = false, name = "status") Application.ApplicationStatus status,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            List<ApplicationResponse> applications = applicationService.searchApplicantsForJob(
                    jobId, name, skill, experience, education, appliedAfter, status, user);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}