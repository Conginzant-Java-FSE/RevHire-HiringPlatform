package com.example.revhirehiringplatform.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobSeekerProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String headline;
    private String summary;
    private String location;
    private String employmentStatus;

    // Resume Details
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;

    private List<SkillResponse> skillsList;

    @Data
    public static class ApplicationStatusHistoryResponse {
        private Long id;
        private String oldStatus;
        private String newStatus;
        private String changedByUserName;
        private String comment;
        private LocalDateTime changedAt;
    }
}
