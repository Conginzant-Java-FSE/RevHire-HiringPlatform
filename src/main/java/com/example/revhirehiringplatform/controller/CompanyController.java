package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.dto.request.CompanyRequest;
import com.example.revhirehiringplatform.dto.response.CompanyResponse;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.Application;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employer/profile")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        return userOpt.orElse(null);
    }

    @PostMapping
    public ResponseEntity<?> updateProfile(@Valid @RequestBody CompanyRequest companyDto,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            CompanyResponse company = companyService.createOrUpdateCompanyProfile(companyDto, user);
            return ResponseEntity.ok(company);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/companies")
    public ResponseEntity<?> getMyCompanies(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        List<CompanyResponse> companies = companyService.getCompaniesForUser(user).stream()
                .map(company -> companyService.getCompanyById(company.getId()))
                .toList();
        return ResponseEntity.ok(companies);
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        CompanyResponse company = companyService.getCompanyProfile(user);
        if (company == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(company);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable Long id) {
        try {
            CompanyResponse company = companyService.getCompanyById(id);
            return ResponseEntity.ok(company);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        List<JobPost> employerJobs = jobPostRepository.findByCreatedBy(user);
        long totalJobs = employerJobs.size();
        long activeJobs = employerJobs.stream()
                .filter(j -> j.getStatus() == JobPost.JobStatus.ACTIVE)
                .count();

        List<Application> employerApplications = applicationRepository.findByJobPostCreatedBy(user);
        long totalApplications = employerApplications.size();
        long pendingReviews = employerApplications.stream()
                .filter(a -> a.getStatus() == Application.ApplicationStatus.UNDER_REVIEW
                        || a.getStatus() == Application.ApplicationStatus.APPLIED)
                .count();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalJobs", totalJobs);
        stats.put("activeJobs", activeJobs);
        stats.put("totalApplications", totalApplications);
        stats.put("pendingReviews", pendingReviews);

        return ResponseEntity.ok(stats);
    }
}
