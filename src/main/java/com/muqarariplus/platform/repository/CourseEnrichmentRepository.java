package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.entity.EnrichmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseEnrichmentRepository extends JpaRepository<CourseEnrichment, Long> {

    /**
     * For students viewing a specific course — returns only approved enrichments.
     */
    List<CourseEnrichment> findByCourseIdAndStatus(Long courseId, EnrichmentStatus status);

    /**
     * Returns ALL enrichments for a course regardless of status (used by purge).
     */
    List<CourseEnrichment> findByCourseId(Long courseId);

    /**
     * Counts enrichments for a course by status (used by catalog badge logic).
     */
    long countByCourseIdAndStatus(Long courseId, EnrichmentStatus status);

    /**
     * For the expert's dashboard — returns all enrichments by the expert's User ID.
     */
    List<CourseEnrichment> findByExpertUserId(Long userId);

    /**
     * For the admin queue — returns enrichments filtered by status (e.g., PENDING).
     */
    List<CourseEnrichment> findByStatus(EnrichmentStatus status);

    /**
     * Global Smart Search: finds APPROVED enrichments where the keyword matches
     * the content, expert's name, associated skill names, or associated tool names.
     * Uses LEFT JOIN to traverse @ManyToMany without N+1 issues.
     * DISTINCT prevents duplicate results from multiple join matches.
     */
    @Query("SELECT DISTINCT e FROM CourseEnrichment e " +
           "LEFT JOIN e.expert exp " +
           "LEFT JOIN exp.user u " +
           "LEFT JOIN e.course c " +
           "LEFT JOIN e.skills s " +
           "LEFT JOIN e.tools t " +
           "WHERE e.status = :status AND (" +
           "  LOWER(e.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(c.nameAr) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(s.nameAr) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(t.nameAr) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(t.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
           ")")
    List<CourseEnrichment> searchApprovedByKeyword(@Param("keyword") String keyword,
                                                   @Param("status") EnrichmentStatus status);

    long countByCourseId(Long courseId);
}
