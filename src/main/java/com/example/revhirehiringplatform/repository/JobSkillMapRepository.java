package com.example.revhirehiringplatform.repository;



import com.revhire.model.JobSkillMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobSkillMapRepository extends JpaRepository<JobSkillMap, Long> {
}
