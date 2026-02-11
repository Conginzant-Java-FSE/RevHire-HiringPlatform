package com.example.revhirehiringplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRequest {
    private Long id;

    @NotBlank(message = "Company Name is required")
    private String name;

    private String description;
    private String website;
    private String location;
    private String industry;
}
