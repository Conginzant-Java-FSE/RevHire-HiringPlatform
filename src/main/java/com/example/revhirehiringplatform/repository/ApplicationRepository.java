package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByJobPostId(Long jobId);

    List<Application> findByJobSeekerId(Long seekerId);

    List<Application> findByJobPostCreatedBy(com.example.revhirehiringplatform.model.User user);
}
