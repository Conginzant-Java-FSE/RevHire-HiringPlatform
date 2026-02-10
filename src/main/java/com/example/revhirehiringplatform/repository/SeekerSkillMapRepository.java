package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.SeekerSkillMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeekerSkillMapRepository extends JpaRepository<SeekerSkillMap, Long> {
}
