package com.revhire.service;

import com.revhire.dto.request.CompanyRequest;
import com.revhire.dto.response.CompanyResponse;
import com.revhire.model.Company;
import com.revhire.model.EmployerProfile;
import com.revhire.model.User;
import com.revhire.repository.CompanyRepository;
import com.revhire.repository.EmployerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final EmployerProfileRepository employerProfileRepository;

    @Transactional
    public CompanyResponse createOrUpdateCompanyProfile(CompanyRequest companyDto, User user) {
        log.info("Creating/Updating company: {} for user: {}", companyDto.getName(), user.getEmail());

        Company company;
        if (companyDto.getId() != null) {
            company = companyRepository.findById(companyDto.getId())
                    .orElseThrow(() -> new RuntimeException("Company not found"));

            // Security check: only creator can update
            if (company.getCreatedBy() != null && !company.getCreatedBy().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to update this company");
            }
        } else {
            // Check if user already has a company
            java.util.List<Company> existingCompanies = companyRepository.findByCreatedByOrderByNameAsc(user);
            if (!existingCompanies.isEmpty()) {
                throw new RuntimeException("You can only have one company profile");
            }
            company = new Company();
            company.setCreatedBy(user);
        }

        company.setName(companyDto.getName());
        company.setDescription(companyDto.getDescription());
        company.setWebsite(companyDto.getWebsite());
        company.setLocation(companyDto.getLocation());
        company.setIndustry(companyDto.getIndustry());
        company.setSize(""); // Default or add to DTO

        company = companyRepository.save(company);

        // Maintain EmployerProfile for backward compatibility or primary company link
        Optional<EmployerProfile> profileOpt = employerProfileRepository.findByUserId(user.getId());
        if (profileOpt.isEmpty()) {
            EmployerProfile newProfile = new EmployerProfile();
            newProfile.setUser(user);
            newProfile.setCompany(company);
            newProfile.setDesignation("HR / Admin");
            employerProfileRepository.save(newProfile);
        }

        return mapToDto(company);
    }

    public java.util.List<Company> getCompaniesForUser(User user) {
        return companyRepository.findByCreatedByOrderByNameAsc(user);
    }

    public CompanyResponse getCompanyProfile(User user) {
        Company company = employerProfileRepository.findByUserId(user.getId())
                .map(EmployerProfile::getCompany)
                .orElse(null); // Return null instead of throw to allow UI to handle no company state
        return company != null ? mapToDto(company) : null;
    }

    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return mapToDto(company);
    }

    private CompanyResponse mapToDto(Company company) {
        CompanyResponse dto = new CompanyResponse();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setWebsite(company.getWebsite());
        dto.setLocation(company.getLocation());
        dto.setIndustry(company.getIndustry());
        return dto;
    }
}
