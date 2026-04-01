package com.muqarariplus.platform.service;

import com.muqarariplus.platform.dto.SearchResultDTO;
import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.entity.EnrichmentStatus;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class SearchService {

    private final CourseRepository courseRepository;
    private final CourseEnrichmentRepository enrichmentRepository;

    public SearchService(CourseRepository courseRepository,
                         CourseEnrichmentRepository enrichmentRepository) {
        this.courseRepository = courseRepository;
        this.enrichmentRepository = enrichmentRepository;
    }

    /**
     * Global Smart Search: searches across Courses and APPROVED Enrichments
     * (including their Skills, Tools, and Expert names) simultaneously.
     */
    @Transactional(readOnly = true)
    public SearchResultDTO searchGlobal(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new SearchResultDTO(Collections.emptyList(), Collections.emptyList());
        }

        String trimmed = keyword.trim();

        // 1. Search Courses
        List<Course> courses = courseRepository.searchByKeyword(trimmed);

        // 2. Search APPROVED Enrichments (deep search across content, experts, skills, tools)
        List<CourseEnrichment> enrichments = enrichmentRepository
                .searchApprovedByKeyword(trimmed, EnrichmentStatus.APPROVED);

        return new SearchResultDTO(courses, enrichments);
    }
}
