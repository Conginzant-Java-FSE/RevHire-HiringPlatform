package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.JobPostRequest;
import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.model.Company;
import com.example.revhirehiringplatform.model.EmployerProfile;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.model.JobSkillMap;
import com.example.revhirehiringplatform.model.SkillsMaster;
import com.example.revhirehiringplatform.model.User;

import com.example.revhirehiringplatform.repository.CompanyRepository;
import com.example.revhirehiringplatform.repository.EmployerProfileRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.JobSkillMapRepository;
import com.example.revhirehiringplatform.repository.SkillsMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobPostRepository jobPostRepository;
    private final CompanyRepository companyRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final AuditLogService auditLogService;
    private final SkillsMasterRepository skillsMasterRepository;
    private final JobSkillMapRepository jobSkillMapRepository;

    @Transactional
    public JobPostResponse createJob(JobPostRequest jobPostDto, User user) {
        log.info("Creating job: {} for user: {}", jobPostDto.getTitle(), user.getEmail());

        if (jobPostDto.getCompanyId() == null) {
            throw new RuntimeException("Company ID is required to post a job");
        }

        Company company = companyRepository.findById(jobPostDto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Security check: only creator of the company or assigned employer can post
        if (company.getCreatedBy() != null && !company.getCreatedBy().getId().equals(user.getId())) {
            // Fallback check EmployerProfile if creator is null (legacy)
            Optional<EmployerProfile> profileOpt = employerProfileRepository.findByUserId(user.getId());
            if (profileOpt.isEmpty() || !profileOpt.get().getCompany().getId().equals(company.getId())) {
                throw new RuntimeException("Unauthorized to post for this company");
            }
        }

        JobPost jobPost = new JobPost();
        jobPost.setTitle(jobPostDto.getTitle());
        jobPost.setDescription(jobPostDto.getDescription());
        jobPost.setLocation(jobPostDto.getLocation());
        jobPost.setSalaryMin(parseSalary(jobPostDto.getSalary(), true));
        jobPost.setSalaryMax(parseSalary(jobPostDto.getSalary(), false));
        jobPost.setJobType(jobPostDto.getJobType());
        jobPost.setDeadline(jobPostDto.getDeadline() != null ? jobPostDto.getDeadline() : LocalDate.now().plusDays(30));
        jobPost.setCompany(company);
        jobPost.setCreatedBy(user);
        jobPost.setStatus(JobPost.JobStatus.ACTIVE);
        JobPost savedJob = jobPostRepository.save(jobPost);

        if (jobPostDto.getRequirements() != null && !jobPostDto.getRequirements().trim().isEmpty()) {
            List<String> skillNames = Arrays.stream(jobPostDto.getRequirements().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            for (String skillName : skillNames) {
                SkillsMaster skillMaster = skillsMasterRepository.findBySkillNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            SkillsMaster master = new SkillsMaster();
                            master.setSkillName(skillName);
                            return skillsMasterRepository.save(master);
                        });
                JobSkillMap skillMap = new JobSkillMap();
                skillMap.setJobPost(savedJob);
                skillMap.setSkill(skillMaster);
                skillMap.setMandatory(true); // Default
                jobSkillMapRepository.save(skillMap);
            }
        }

        auditLogService.logAction(
                "JobPost",
                savedJob.getId(),
                "JOB_CREATED",
                null,
                "Title: " + savedJob.getTitle(),
                user);

        return mapToDto(savedJob);
    }

    // Helper to parse legacy "salary" string to min/max
    private Double parseSalary(String salary, boolean isMin) {
        if (salary == null || salary.isEmpty())
            return 0.0;
        try {
            String[] parts = salary.split("-");
            if (parts.length > 0) {
                String val = parts[isMin ? 0 : (parts.length > 1 ? 1 : 0)].replaceAll("[^0-9.]", "");
                return val.isEmpty() ? 0.0 : Double.parseDouble(val);
            }
            return 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public List<JobPostResponse> getAllJobs() {
        return jobPostRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public JobPost getJobById(Long id) {
        return jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    public List<JobPostResponse> getMyJobs(User user) {
        // Return jobs created by this user
        return jobPostRepository.findByCreatedBy(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<JobPostResponse> getRecommendedJobs(User user) {
        log.info("Getting recommendations for user: {}", user.getEmail());
        // Simple recommendation: return top 10 active jobs
        return jobPostRepository.findAll().stream()
                .filter(j -> j.getStatus() == JobPost.JobStatus.ACTIVE)
                .limit(10)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public JobPostResponse updateJob(Long id, JobPostRequest jobDto, User user) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only edit jobs you posted.");
        }

        job.setTitle(jobDto.getTitle());
        job.setDescription(jobDto.getDescription());
        job.setLocation(jobDto.getLocation());
        job.setSalaryMin(parseSalary(jobDto.getSalary(), true));
        job.setSalaryMax(parseSalary(jobDto.getSalary(), false));
        job.setJobType(jobDto.getJobType());
        if (jobDto.getDeadline() != null)
            job.setDeadline(jobDto.getDeadline());
        if (jobDto.getExperienceYears() != null)
            job.setExperienceYears(jobDto.getExperienceYears());

        JobPost updatedJob = jobPostRepository.save(job);

        if (jobDto.getRequirements() != null && !jobDto.getRequirements().trim().isEmpty()) {
            List<String> skillNames = Arrays.stream(jobDto.getRequirements().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            List<JobSkillMap> existingMaps = jobSkillMapRepository.findByJobPostId(updatedJob.getId());
            jobSkillMapRepository.deleteAll(existingMaps);

            for (String skillName : skillNames) {
                SkillsMaster skillMaster = skillsMasterRepository.findBySkillNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            SkillsMaster master = new SkillsMaster();
                            master.setSkillName(skillName);
                            return skillsMasterRepository.save(master);
                        });
                JobSkillMap skillMap = new JobSkillMap();
                skillMap.setJobPost(updatedJob);
                skillMap.setSkill(skillMaster);
                skillMap.setMandatory(true);
                jobSkillMapRepository.save(skillMap);
            }
        }

        auditLogService.logAction(
                "JobPost",
                updatedJob.getId(),
                "JOB_UPDATED",
                "Old Title: " + job.getTitle() + ", Old Type: " + job.getJobType(),
                "New Title: " + updatedJob.getTitle() + ", New Type: " + updatedJob.getJobType(),
                user);

        return mapToDto(updatedJob);
    }

    public JobPostResponse updateJobStatus(Long id, JobPost.JobStatus status, User user) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only edit jobs you posted.");
        }

        job.setStatus(status);
        JobPost updatedJob = jobPostRepository.save(job);

        auditLogService.logAction(
                "JobPost",
                updatedJob.getId(),
                "JOB_STATUS_UPDATED",
                "Old Status: " + job.getStatus(),
                "New Status: " + updatedJob.getStatus(),
                user);

        return mapToDto(updatedJob);
    }

    public void deleteJob(Long id, User user) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only delete jobs you posted.");
        }

        auditLogService.logAction(
                "JobPost",
                job.getId(),
                "JOB_DELETED",
                "Title: " + job.getTitle(),
                null,
                user);

        jobPostRepository.delete(job);
    }

    public JobPostResponse mapToDto(JobPost jobPost) {
        JobPostResponse dto = new JobPostResponse();
        dto.setId(jobPost.getId());
        dto.setTitle(jobPost.getTitle());
        dto.setDescription(jobPost.getDescription());

        List<JobSkillMap> skills = jobSkillMapRepository.findByJobPostId(jobPost.getId());
        if (!skills.isEmpty()) {
            dto.setRequirements(
                    skills.stream().map(s -> s.getSkill().getSkillName()).collect(Collectors.joining(", ")));
        } else {
            dto.setRequirements(jobPost.getDescription()); // Fallback
        }

        dto.setLocation(jobPost.getLocation());
        dto.setSalary(jobPost.getSalaryMin() + " - " + jobPost.getSalaryMax());
        dto.setJobType(jobPost.getJobType());
        dto.setPostedDate(jobPost.getCreatedAt() != null ? jobPost.getCreatedAt().toLocalDate() : LocalDate.now());
        dto.setCompanyId(jobPost.getCompany().getId());
        dto.setCompanyName(jobPost.getCompany().getName());
        return dto;
    }
}
