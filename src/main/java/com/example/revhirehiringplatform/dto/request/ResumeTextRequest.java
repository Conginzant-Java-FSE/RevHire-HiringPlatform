package com.revhire.dto.request;

import lombok.Data;

@Data
public class ResumeTextRequest {
    private String objective;
    private String education;
    private String experience;
    private String skills;
    private String projects;
    private String certifications;
}
