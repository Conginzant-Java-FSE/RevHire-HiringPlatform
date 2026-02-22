package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.response.JobSeekerProfileResponse;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.SavedResume;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.repository.SavedResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedResumeService {

    private final SavedResumeRepository savedResumeRepository;
    private final JobSeekerProfileRepository profileRepository;
    private final NotificationService notificationService;

    @Transactional
    public void saveResume(Long seekerId, User employer) {
        log.info("Employer {} saving resume for job seeker {}", employer.getEmail(), seekerId);

        JobSeekerProfile profile = profileRepository.findById(seekerId)
                .orElseThrow(() -> new RuntimeException("Job Seeker profile not found"));

        if (savedResumeRepository.existsByEmployerIdAndJobSeekerId(employer.getId(), profile.getId())) {
            throw new RuntimeException("Resume already saved by this employer");
        }

        SavedResume savedResume = new SavedResume();
        savedResume.setEmployer(employer);
        savedResume.setJobSeeker(profile);
        savedResumeRepository.save(savedResume);

        notificationService.createNotification(
                profile.getUser(),
                "An employer has favorited your profile!");
    }

    @Transactional
    public void unsaveResume(Long seekerId, User employer) {
        log.info("Employer {} unsaving resume for job seeker {}", employer.getEmail(), seekerId);

        JobSeekerProfile profile = profileRepository.findById(seekerId)
                .orElseThrow(() -> new RuntimeException("Job Seeker profile not found"));

        SavedResume savedResume = savedResumeRepository
                .findByEmployerIdAndJobSeekerId(employer.getId(), profile.getId())
                .orElseThrow(() -> new RuntimeException("Saved resume not found"));

        savedResumeRepository.delete(savedResume);
    }

    @Transactional(readOnly = true)
    public List<JobSeekerProfileResponse> getSavedResumes(User employer) {
        log.info("Fetching saved resumes for employer {}", employer.getEmail());

        return savedResumeRepository.findByEmployerId(employer.getId()).stream()
                .map(savedResume -> {
                    JobSeekerProfile profile = savedResume.getJobSeeker();

                    JobSeekerProfileResponse dto = new JobSeekerProfileResponse();
                    dto.setHeadline(profile.getHeadline());
                    dto.setSummary(profile.getSummary());
                    dto.setLocation(profile.getLocation());
                    dto.setEmploymentStatus(profile.getEmploymentStatus());

                    if (profile.getUser() != null) {
                        dto.setPhone(profile.getUser().getPhone());
                    }

                    return dto;
                })
                .toList();
    }
}