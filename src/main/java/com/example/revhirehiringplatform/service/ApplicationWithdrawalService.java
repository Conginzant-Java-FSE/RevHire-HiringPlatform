package com.revhire.service;

import com.revhire.dto.response.ApplicationResponse;
import com.revhire.model.Application;
import com.revhire.model.User;
import com.revhire.model.ApplicationStatusHistory;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.ApplicationStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationWithdrawalService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;

    @Transactional
    public ApplicationResponse withdrawApplication(Long applicationId, String reason, User user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getJobSeeker().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to withdraw this application");
        }

        Application.ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(Application.ApplicationStatus.WITHDRAWN);
        application.setWithdrawReason(reason);
        Application savedApp = applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(savedApp);
        history.setOldStatus(oldStatus != null ? oldStatus.name() : "");
        history.setNewStatus(Application.ApplicationStatus.WITHDRAWN.name());
        history.setChangedBy(user);
        history.setComment("Application withdrawn by job seeker: " + reason);
        statusHistoryRepository.save(history);

        return mapToDto(savedApp);
    }

    private ApplicationResponse mapToDto(Application app) {
        ApplicationResponse dto = new ApplicationResponse();
        dto.setId(app.getId());
        dto.setJobId(app.getJobPost().getId());
        dto.setJobTitle(app.getJobPost().getTitle());
        dto.setCompanyName(app.getJobPost().getCompany().getName());
        dto.setJobSeekerId(app.getJobSeeker().getId());
        dto.setJobSeekerName(app.getJobSeeker().getUser().getName());
        dto.setJobSeekerEmail(app.getJobSeeker().getUser().getEmail());
        dto.setJobSeekerSkills(app.getJobSeeker().getSummary()); // Using summary as placeholder for skills
        dto.setStatus(app.getStatus());
        dto.setAppliedAt(app.getAppliedAt());
        return dto;
    }
}
