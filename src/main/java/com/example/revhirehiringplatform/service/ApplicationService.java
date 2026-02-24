package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.ApplicationResponse;
import com.example.revhirehiringplatform.model.Application;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.ApplicationRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.ApplicationStatusHistory;
import com.example.revhirehiringplatform.model.SeekerSkillMap;
import com.example.revhirehiringplatform.repository.ResumeTextRepository;
import com.example.revhirehiringplatform.repository.SeekerSkillMapRepository;
import com.example.revhirehiringplatform.repository.ApplicationStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

        private final ApplicationRepository applicationRepository;
        private final JobPostRepository jobPostRepository;
        private final JobSeekerProfileRepository profileRepository;
        private final ResumeTextRepository resumeTextRepository;
        private final SeekerSkillMapRepository seekerSkillMapRepository;
        private final ApplicationStatusHistoryRepository statusHistoryRepository;
        private final NotificationService notificationService;
        private final AuditLogService auditLogService;

        @Transactional
        public ApplicationResponse applyForJob(Long jobId, User user) {
                log.info("User {} applying for job {}", user.getEmail(), jobId);
                JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new RuntimeException("Complete your profile before applying"));

                JobPost jobPost = jobPostRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                // Check if already applied
                boolean alreadyApplied = applicationRepository.findByJobSeekerId(profile.getId()).stream()
                                .anyMatch(app -> app.getJobPost().getId().equals(jobId));

                if (alreadyApplied) {
                        throw new RuntimeException("You have already applied for this job");
                }

                Application application = new Application();
                application.setJobPost(jobPost);
                application.setJobSeeker(profile);
                // application.setAppliedAt(LocalDateTime.now()); // Handled by
                // @CreationTimestamp
                application.setStatus(Application.ApplicationStatus.APPLIED);

                Application savedApp = applicationRepository.save(application);

                // Notify Seeker
                notificationService.createNotification(user,
                                "You have successfully applied for the " + jobPost.getTitle() + " position at "
                                                + jobPost.getCompany().getName());

                auditLogService.logAction(
                                "Application",
                                savedApp.getId(),
                                "APPLICATION_SUBMITTED",
                                null,
                                "Job: " + jobPost.getTitle() + ", Applicant: " + profile.getUser().getName(),
                                user);

                return mapToDto(savedApp);
        }

        @Transactional(readOnly = true)
        public List<ApplicationResponse> getMyApplications(User user) {
                JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new RuntimeException("Profile not found"));
                return applicationRepository.findByJobSeekerId(profile.getId()).stream()
                                .map(this::mapToDto)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ApplicationResponse> getApplicationsForJob(Long jobId, User employer) {
                JobPost jobPost = jobPostRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                if (!jobPost.getCreatedBy().getId().equals(employer.getId())) {
                        throw new RuntimeException("Unauthorized to view these applications");
                }

                return applicationRepository.findByJobPostId(jobId).stream()
                                .map(this::mapToDto)
                                .toList();
        }

        @Transactional
        public ApplicationResponse updateApplicationStatus(Long applicationId, Application.ApplicationStatus status,
                        User employer) {
                Application application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new RuntimeException("Application not found"));

                if (!application.getJobPost().getCreatedBy().getId().equals(employer.getId())) {
                        throw new RuntimeException("Unauthorized to update this application");
                }

                Application.ApplicationStatus oldStatus = application.getStatus();
                application.setStatus(status);
                Application savedApp = applicationRepository.save(application);

                ApplicationStatusHistory history = new ApplicationStatusHistory();
                history.setApplication(savedApp);
                history.setOldStatus(oldStatus != null ? oldStatus.name() : "");
                history.setNewStatus(status.name());
                history.setChangedBy(employer);
                history.setComment("Status updated by employer");
                statusHistoryRepository.save(history);

                // Notify Seeker
                notificationService.createNotification(
                                application.getJobSeeker().getUser(),
                                "Your application for " + application.getJobPost().getTitle() + " has been updated to "
                                                + status);

                return mapToDto(savedApp);
        }

        @Transactional
        public List<ApplicationResponse> updateBulkStatus(List<Long> applicationIds,
                        Application.ApplicationStatus status,
                        User employer) {
                List<Application> applications = applicationRepository.findAllById(applicationIds);

                for (Application app : applications) {
                        if (!app.getJobPost().getCreatedBy().getId().equals(employer.getId())) {
                                throw new RuntimeException("Unauthorized to update application " + app.getId());
                        }
                        Application.ApplicationStatus oldStatus = app.getStatus();
                        app.setStatus(status);

                        Application savedApp = applicationRepository.save(app);

                        ApplicationStatusHistory history = new ApplicationStatusHistory();
                        history.setApplication(savedApp);
                        history.setOldStatus(oldStatus != null ? oldStatus.name() : "");
                        history.setNewStatus(status.name());
                        history.setChangedBy(employer);
                        history.setComment("Bulk status updated by employer");
                        statusHistoryRepository.save(history);

                        // Notify Seeker
                        notificationService.createNotification(
                                        app.getJobSeeker().getUser(),
                                        "Your application for " + app.getJobPost().getTitle() + " has been updated to "
                                                        + status);
                }

                return applications.stream().map(this::mapToDto).toList();
        }

        @Transactional(readOnly = true)
        public List<ApplicationResponse> searchApplicantsForJob(Long jobId, String name, String skill,
                        String experience, String education, String appliedAfter,
                        Application.ApplicationStatus status, User employer) {
                JobPost jobPost = jobPostRepository.findById(jobId)
                                .orElseThrow(() -> new RuntimeException("Job not found"));

                if (!jobPost.getCreatedBy().getId().equals(employer.getId())) {
                        throw new RuntimeException("Unauthorized to view these applications");
                }

                List<Application> applications = applicationRepository.findByJobPostId(jobId);

                // Filter on Application properties (Name, status, appliedAfter)
                if (name != null && !name.trim().isEmpty()) {
                        applications = applications.stream()
                                        .filter(app -> app.getJobSeeker().getUser().getName().toLowerCase()
                                                        .contains(name.toLowerCase()))
                                        .toList();
                }

                if (status != null) {
                        applications = applications.stream()
                                        .filter(app -> app.getStatus() == status)
                                        .toList();
                }

                if (appliedAfter != null && !appliedAfter.trim().isEmpty()) {
                        LocalDateTime afterDate = LocalDateTime.parse(appliedAfter, DateTimeFormatter.ISO_DATE_TIME);
                        applications = applications.stream()
                                        .filter(app -> app.getAppliedAt() != null
                                                        && app.getAppliedAt().isAfter(afterDate))
                                        .toList();
                }

                // Map to DTO, which fetches ResumeText and Relational Skills
                List<ApplicationResponse> applicationDtos = applications.stream()
                                .map(this::mapToDto)
                                .toList();

                // Filter on ResumeText properties (Skills, experience, education)
                if (skill != null && !skill.trim().isEmpty()) {
                        applicationDtos = applicationDtos.stream()
                                        .filter(dto -> dto.getJobSeekerSkills() != null
                                                        && dto.getJobSeekerSkills().toLowerCase()
                                                                        .contains(skill.toLowerCase()))
                                        .toList();
                }

                if (experience != null && !experience.trim().isEmpty()) {
                        applicationDtos = applicationDtos.stream()
                                        .filter(dto -> dto.getJobSeekerExperience() != null
                                                        && dto.getJobSeekerExperience().toLowerCase()
                                                                        .contains(experience.toLowerCase()))
                                        .toList();
                }

                if (education != null && !education.trim().isEmpty()) {
                        applicationDtos = applicationDtos.stream()
                                        .filter(dto -> dto.getJobSeekerEducation() != null
                                                        && dto.getJobSeekerEducation().toLowerCase()
                                                                        .contains(education.toLowerCase()))
                                        .toList();
                }

                return applicationDtos;
        }

        private ApplicationResponse mapToDto(Application app) {
                ApplicationResponse dto = new ApplicationResponse();
                dto.setId(app.getId());
                dto.setJobId(app.getJobPost().getId());
                dto.setJobTitle(app.getJobPost().getTitle());
                dto.setCompanyName(app.getJobPost().getCompany().getName());
                dto.setJobSeekerId(app.getJobSeeker().getId());
                dto.setJobSeekerName(app.getJobSeeker().getUser().getName());
                dto.setJobSeekerEmail(app.getJobSeeker().getUser().getEmail());

                // Fetch skills from the new relational mapping
                List<SeekerSkillMap> skills = seekerSkillMapRepository.findByJobSeekerId(app.getJobSeeker().getId());
                if (!skills.isEmpty()) {
                        dto.setJobSeekerSkills(skills.stream()
                                        .map(s -> s.getSkill().getSkillName())
                                        .collect(Collectors.joining(", ")));
                }

                ResumeText resumeText = resumeTextRepository.findByJobSeekerId(app.getJobSeeker().getId()).orElse(null);
                if (resumeText != null) {
                        // Keep ResumeText for fallback / other fields
                        if (dto.getJobSeekerSkills() == null) {
                                dto.setJobSeekerSkills(resumeText.getSkillsText());
                        }
                        dto.setJobSeekerExperience(resumeText.getExperienceText());
                        dto.setJobSeekerEducation(resumeText.getEducationText());
                }

                dto.setStatus(app.getStatus());
                dto.setAppliedAt(app.getAppliedAt());
                return dto;
        }
}
