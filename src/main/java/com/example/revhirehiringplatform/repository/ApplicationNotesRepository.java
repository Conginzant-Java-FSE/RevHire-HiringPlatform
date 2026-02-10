package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.ApplicationNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationNotesRepository extends JpaRepository<ApplicationNotes, Long> {
}