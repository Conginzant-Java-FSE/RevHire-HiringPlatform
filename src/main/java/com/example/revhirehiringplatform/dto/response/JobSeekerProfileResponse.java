package com.example.revhirehiringplatform.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class JobSeekerProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
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
}
