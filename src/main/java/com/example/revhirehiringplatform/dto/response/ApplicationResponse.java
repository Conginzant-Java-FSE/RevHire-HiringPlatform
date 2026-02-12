package com.example.revhirehiringplatform.dto.response;

import com.example.revhirehiringplatform.model.Application.ApplicationStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private Long jobSeekerId;
    private String jobSeekerName;
    private String jobSeekerEmail;
    private String jobSeekerSkills;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}