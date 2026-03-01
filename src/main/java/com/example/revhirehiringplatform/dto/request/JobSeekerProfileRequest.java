package com.revhire.dto.request;

import lombok.Data;

@Data
public class JobSeekerProfileRequest {
    private String headline;
    private String summary;
    private String location;
    private String phone;
    private String employmentStatus; // FRESHER, EMPLOYED, UNEMPLOYED

    // Resume Text Fields
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;
}
