package com.revhire.repository;

import com.revhire.model.SavedJobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobsRepository extends JpaRepository<SavedJobs, Long> {
    List<SavedJobs> findByJobSeekerId(Long seekerId);

    Optional<SavedJobs> findByJobSeekerIdAndJobPostId(Long seekerId, Long jobPostId);
}
