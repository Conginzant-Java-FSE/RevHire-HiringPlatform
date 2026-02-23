//package com.example.revhirehiringplatform.repository;
//
//import com.example.revhirehiringplatform.model.AuditLog;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
//    List<AuditLog> findByUserIdOrderByChangedAtDesc(Long userId);
//    List<AuditLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, Long entityId);
//}


package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByChangedBy_IdOrderByChangedAtDesc(Long userId);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(
            String entityType,
            Long entityId
    );
}