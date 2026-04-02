package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Paginated audit trail ordered by timestamp desc */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /** Find all logs by actor email */
    List<AuditLog> findByActorEmailOrderByTimestampDesc(String actorEmail);

    /** Find all logs by action type */
    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    /** Find all logs for a specific entity */
    List<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, String entityId);
}
