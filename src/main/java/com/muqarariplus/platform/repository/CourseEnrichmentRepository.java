package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.CourseEnrichment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseEnrichmentRepository extends JpaRepository<CourseEnrichment, Long> {
    List<CourseEnrichment> findByCourseIdAndVerificationStatus(Long courseId, String status);
    List<CourseEnrichment> findByExpertId(Long expertId);
    List<CourseEnrichment> findByVerificationStatus(String status);
}
