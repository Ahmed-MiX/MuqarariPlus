package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.AuditLog;
import com.muqarariplus.platform.entity.EnrichmentStatus;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.AuditLogRepository;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SuperAdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrichmentRepository enrichmentRepository;
    private final AuditLogRepository auditLogRepository;

    public SuperAdminDashboardService(UserRepository userRepository,
                                      CourseRepository courseRepository,
                                      CourseEnrichmentRepository enrichmentRepository,
                                      AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrichmentRepository = enrichmentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /** Total user count */
    public long getTotalUsers() { return userRepository.count(); }

    /** Users grouped by role */
    public Map<String, Long> getUsersByRole() {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("ROLE_STUDENT", (long) userRepository.findByRole("ROLE_STUDENT").size());
        map.put("ROLE_EXPERT", (long) userRepository.findByRole("ROLE_EXPERT").size());
        map.put("ROLE_ADMIN", (long) userRepository.findByRole("ROLE_ADMIN").size());
        map.put("ROLE_SUPER_ADMIN", (long) userRepository.findByRole("ROLE_SUPER_ADMIN").size());
        return map;
    }

    /** Total courses */
    public long getTotalCourses() { return courseRepository.count(); }

    /** Enrichments grouped by status */
    public Map<String, Long> getEnrichmentsByStatus() {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("APPROVED", (long) enrichmentRepository.findByStatus(EnrichmentStatus.APPROVED).size());
        map.put("PENDING", (long) enrichmentRepository.findByStatus(EnrichmentStatus.PENDING).size());
        map.put("REJECTED", (long) enrichmentRepository.findByStatus(EnrichmentStatus.REJECTED).size());
        return map;
    }

    /** Total enrichments */
    public long getTotalEnrichments() { return enrichmentRepository.count(); }

    /** Latest 50 audit log entries */
    public List<AuditLog> getLatestAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 50)).getContent();
    }

    /** All users */
    public List<User> getAllUsers() { return userRepository.findAll(); }
}
