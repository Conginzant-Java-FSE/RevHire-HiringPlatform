package com.example.revhirehiringplatform.controller;


import com.example.revhirehiringplatform.dto.request.JobSeekerProfileRequest;
import com.example.revhirehiringplatform.dto.request.ResumeTextRequest;
import com.example.revhirehiringplatform.dto.response.JobSeekerProfileResponse;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.UserRepository;
import com.example.revhirehiringplatform.security.UserDetailsImpl;
import com.example.revhirehiringplatform.service.JobSeekerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/seeker/profile")
@RequiredArgsConstructor
@Slf4j
public class JobSeekerProfileController {

    private final JobSeekerProfileService profileService;
    private final com.example.revhirehiringplatform.service.JobSeekerResumeService resumeService;
    private final UserRepository userRepository;

    private User getUserFromContext(UserDetailsImpl userDetails) {
        if (userDetails == null)
            return null;
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        return userOpt.orElse(null);
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<?> updateProfile(
            @RequestPart("profile") JobSeekerProfileRequest profileDto,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.updateProfile(profileDto, resume, user);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(consumes = { "application/json" })
    public ResponseEntity<?> updateProfileJson(
            @RequestBody JobSeekerProfileRequest profileDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            // Null for resume file since this is a JSON-only request
            JobSeekerProfile profile = profileService.updateProfile(profileDto, null, user);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.getProfile(user);

            JobSeekerProfileResponse dto = new JobSeekerProfileResponse();
            dto.setHeadline(profile.getHeadline());
            dto.setSummary(profile.getSummary());
            dto.setLocation(profile.getLocation());
            if (profile.getUser() != null) {
                dto.setPhone(profile.getUser().getPhone());
            }
            dto.setEmploymentStatus(profile.getEmploymentStatus());

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/resume-text")
    public ResponseEntity<?> getResumeText(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            JobSeekerProfile profile = profileService.getProfile(user);
            ResumeText resumeText = profileService.getResumeText(profile.getId());

            ResumeTextRequest dto = new ResumeTextRequest();
            if (resumeText != null) {
                dto.setObjective(resumeText.getObjective());
                dto.setEducation(resumeText.getEducationText());
                dto.setExperience(resumeText.getExperienceText());
                dto.setSkills(resumeText.getSkillsText());
                dto.setProjects(resumeText.getProjectsText());
                dto.setCertifications(resumeText.getCertificationsText());
            }

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/resume-text")
    public ResponseEntity<?> updateResumeText(
            @RequestBody ResumeTextRequest textDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.JOB_SEEKER) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        try {
            ResumeText savedText = profileService.updateResumeText(textDto, user);
            return ResponseEntity.ok(savedText);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{seekerId}")
    public ResponseEntity<?> getProfileById(@PathVariable("seekerId") Long seekerId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized: Only Employers can view full profiles.");
        }
        try {
            JobSeekerProfile profile = profileService.getProfileById(seekerId);

            JobSeekerProfileResponse dto = new JobSeekerProfileResponse();
            dto.setHeadline(profile.getHeadline());
            dto.setSummary(profile.getSummary());
            dto.setLocation(profile.getLocation());
            if (profile.getUser() != null) {
                dto.setPhone(profile.getUser().getPhone());
            }
            dto.setEmploymentStatus(profile.getEmploymentStatus());

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{seekerId}/resume/download")
    public ResponseEntity<?> downloadResume(@PathVariable("seekerId") Long seekerId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = getUserFromContext(userDetails);
        if (user == null || user.getRole() != User.Role.EMPLOYER) {
            return ResponseEntity.status(403).body("Unauthorized: Only Employers can download resumes.");
        }
        try {
            com.example.revhirehiringplatform.model.ResumeFiles resumeFile = resumeService.getResumeFile(seekerId);
            if (resumeFile == null) {
                return ResponseEntity.notFound().build(); // No active resume found
            }

            java.nio.file.Path filePath = java.nio.file.Paths.get("uploads/resumes").resolve(resumeFile.getFilePath())
                    .normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(
                    filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build(); // File missing on disk
            }

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resumeFile.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading resume", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
