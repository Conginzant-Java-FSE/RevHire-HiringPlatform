package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.SavedResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedResumeRepository extends JpaRepository<SavedResume, Long> {

    List<SavedResume> findByEmployerId(Long employerId);

    Optional<SavedResume> findByEmployerIdAndJobSeekerId(Long employerId, Long seekerId);

    boolean existsByEmployerIdAndJobSeekerId(Long employerId, Long seekerId);
}
