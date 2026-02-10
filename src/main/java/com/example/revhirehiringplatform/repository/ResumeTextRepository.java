package com.revhire.repository;

import com.revhire.model.ResumeText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ResumeTextRepository extends JpaRepository<ResumeText, Long> {
    Optional<ResumeText> findByJobSeekerId(Long seekerId);
}
