package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.JobSeekerProfileRequest;
import com.example.revhirehiringplatform.dto.request.ResumeTextRequest;
import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeText;
import com.example.revhirehiringplatform.model.SeekerSkillMap;
import com.example.revhirehiringplatform.model.SkillsMaster;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.JobSeekerProfileRepository;
import com.example.revhirehiringplatform.repository.ResumeTextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSeekerProfileService {

    private final JobSeekerProfileRepository profileRepository;
    private final ResumeTextRepository resumeTextRepository;
    private final JobSeekerResumeService resumeService;
    private final com.example.revhirehiringplatform.repository.UserRepository userRepository;
    private final com.example.revhirehiringplatform.repository.SkillsMasterRepository skillsMasterRepository;
    private final com.example.revhirehiringplatform.repository.SeekerSkillMapRepository seekerSkillMapRepository;

    @Transactional
    public JobSeekerProfile updateProfile(JobSeekerProfileRequest profileDto, MultipartFile resumeFile, User user) {
        log.info("Updating profile for user: {}", user.getEmail());
        JobSeekerProfile profile = profileRepository.findByUserId(user.getId()).orElse(new JobSeekerProfile());

        if (profile.getUser() == null) {
            profile.setUser(user);
        }

        // Update User explicitly if phone is provided
        if (profileDto.getPhone() != null) {
            user.setPhone(profileDto.getPhone());
            userRepository.save(user);
        }

        // Update basic profile
        profile.setHeadline(profileDto.getHeadline());
        profile.setSummary(profileDto.getSummary());
        profile.setLocation(profileDto.getLocation());
        profile.setEmploymentStatus(profileDto.getEmploymentStatus());

        JobSeekerProfile savedProfile = profileRepository.save(profile);

        // Handle Resume File
        if (resumeFile != null && !resumeFile.isEmpty()) {
            resumeService.storeFile(resumeFile, savedProfile);
        }

        return savedProfile;
    }

    @Transactional
    public ResumeText updateResumeText(ResumeTextRequest textDto, User user) {
        log.info("Updating resume text for user: {}", user.getEmail());
        JobSeekerProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found. Please create a profile first."));

        ResumeText resumeText = resumeTextRepository.findByJobSeekerId(profile.getId()).orElse(new ResumeText());
        resumeText.setJobSeeker(profile);
        resumeText.setObjective(textDto.getObjective());
        resumeText.setEducationText(textDto.getEducation());
        resumeText.setExperienceText(textDto.getExperience());
        resumeText.setSkillsText(textDto.getSkills());
        resumeText.setProjectsText(textDto.getProjects());
        resumeText.setCertificationsText(textDto.getCertifications());

        resumeText.setCertificationsText(textDto.getCertifications());

        // Process Skills for Relational Mapping
        if (textDto.getSkills() != null && !textDto.getSkills().trim().isEmpty()) {
            // Basic parsing: split by comma
            List<String> skillNames = Arrays.stream(textDto.getSkills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            // Clear old mappings to prevent duplicates (simplest update strategy)
            List<SeekerSkillMap> existingMaps = seekerSkillMapRepository.findByJobSeekerId(profile.getId());
            seekerSkillMapRepository.deleteAll(existingMaps);

            for (String skillName : skillNames) {
                // Find or create in SkillsMaster
                SkillsMaster skillMaster = skillsMasterRepository.findBySkillNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            SkillsMaster master = new SkillsMaster();
                            master.setSkillName(skillName);
                            return skillsMasterRepository.save(master);
                        });

                // Create Mapping
                SeekerSkillMap skillMap = new SeekerSkillMap();
                skillMap.setJobSeeker(profile);
                skillMap.setSkill(skillMaster);
                skillMap.setLevel(SeekerSkillMap.SkillLevel.INTERMEDIATE); // Defaulting to intermediate
                seekerSkillMapRepository.save(skillMap);
            }
        }

        return resumeTextRepository.save(resumeText);
    }

    public JobSeekerProfile getProfile(User user) {
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public ResumeText getResumeText(Long profileId) {
        return resumeTextRepository.findByJobSeekerId(profileId).orElse(null);
    }

    public JobSeekerProfile getProfileById(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }
}
