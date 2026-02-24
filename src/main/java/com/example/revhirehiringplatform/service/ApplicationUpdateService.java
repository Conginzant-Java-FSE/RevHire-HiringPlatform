package com.revhire.service;

import com.revhire.model.Application;
import com.revhire.model.ApplicationNotes;
import com.revhire.model.User;
import com.revhire.repository.ApplicationRepository;
import com.revhire.repository.ApplicationNotesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationUpdateService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationNotesRepository notesRepository;

    @Transactional
    public com.revhire.dto.response.ApplicationNoteResponse addNoteToApplication(Long applicationId, String noteText,
            User employer) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getJobPost().getCreatedBy().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized to add note to this application");
        }

        ApplicationNotes note = new ApplicationNotes();
        note.setApplication(application);
        note.setNoteText(noteText);
        note.setCreatedBy(employer);

        ApplicationNotes savedNote = notesRepository.save(note);
        return mapToDto(savedNote);
    }

    private com.revhire.dto.response.ApplicationNoteResponse mapToDto(ApplicationNotes note) {
        com.revhire.dto.response.ApplicationNoteResponse dto = new com.revhire.dto.response.ApplicationNoteResponse();
        dto.setId(note.getId());
        dto.setNoteText(note.getNoteText());
        dto.setCreatedByUserId(note.getCreatedBy().getId());
        dto.setCreatedByUserName(note.getCreatedBy().getName());
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
    }
}
