package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.model.JobPost;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSearchService {

    private final JobPostRepository jobPostRepository;

    @Transactional(readOnly = true)
    public List<JobPostResponse> searchJobs(String title, String location, Integer experience, String company,
                                            Double salary,
                                            String jobType, Integer daysAgo) {
        java.time.LocalDateTime startDate = null;
        if (daysAgo != null) {
            startDate = java.time.LocalDateTime.now().minusDays(daysAgo);
        }
        return jobPostRepository.findByFilters(title, location, experience, company, salary, jobType, startDate)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private JobPostResponse mapToDto(JobPost jobPost) {
        JobPostResponse dto = new JobPostResponse();
        dto.setId(jobPost.getId());
        dto.setTitle(jobPost.getTitle());
        dto.setDescription(jobPost.getDescription());
        dto.setRequirements(jobPost.getDescription());
        dto.setLocation(jobPost.getLocation());
        dto.setSalary(jobPost.getSalaryMin() + " - " + jobPost.getSalaryMax());
        dto.setJobType(jobPost.getJobType());
        dto.setPostedDate(jobPost.getCreatedAt() != null ? jobPost.getCreatedAt().toLocalDate() : LocalDate.now());
        dto.setCompanyId(jobPost.getCompany().getId());
        dto.setCompanyName(jobPost.getCompany().getName());
        return dto;
    }
}
