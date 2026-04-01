package com.muqarariplus.platform.service;

import com.muqarariplus.platform.dto.ChartDataDTO;
import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.entity.EnrichmentStatus;
import com.muqarariplus.platform.entity.Skill;
import com.muqarariplus.platform.entity.Tool;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.ExpertRepository;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.entity.ExpertStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final CourseEnrichmentRepository enrichmentRepository;
    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;

    public AnalyticsService(CourseEnrichmentRepository enrichmentRepository,
                            UserRepository userRepository,
                            ExpertRepository expertRepository) {
        this.enrichmentRepository = enrichmentRepository;
        this.userRepository = userRepository;
        this.expertRepository = expertRepository;
    }

    /**
     * Total number of registered students on the platform.
     */
    public long getTotalStudentsCount() {
        return userRepository.countByRole("ROLE_STUDENT");
    }

    /**
     * Total number of verified (APPROVED) experts.
     */
    public long getVerifiedExpertsCount() {
        return expertRepository.findByStatus(ExpertStatus.APPROVED).size();
    }

    /**
     * Total number of APPROVED enrichments across the platform.
     */
    public long getApprovedEnrichmentsCount() {
        return enrichmentRepository.findByStatus(EnrichmentStatus.APPROVED).size();
    }

    /**
     * Total number of PENDING enrichments awaiting moderation.
     */
    public long getPendingEnrichmentsCount() {
        return enrichmentRepository.findByStatus(EnrichmentStatus.PENDING).size();
    }

    /**
     * Total courses on the platform.
     */
    public long getTotalCoursesCount() {
        return enrichmentRepository.count();
    }

    /**
     * Aggregates the most demanded Skills from ALL APPROVED enrichments.
     * Groups by skill name (Arabic), counts frequency, sorts descending, limits to top N.
     * Uses Java Streams to safely traverse the @ManyToMany relationship.
     */
    @Transactional(readOnly = true)
    public List<ChartDataDTO> getTopSkills(int limit) {
        List<CourseEnrichment> approved = enrichmentRepository.findByStatus(EnrichmentStatus.APPROVED);

        Map<String, Long> skillFrequency = approved.stream()
                .flatMap(e -> e.getSkills().stream())
                .collect(Collectors.groupingBy(
                        Skill::getNameAr,
                        Collectors.counting()
                ));

        return skillFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> new ChartDataDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Aggregates the most used Tools from ALL APPROVED enrichments.
     * Groups by tool name (English for recognizable brand names), counts frequency,
     * sorts descending, limits to top N.
     */
    @Transactional(readOnly = true)
    public List<ChartDataDTO> getTopTools(int limit) {
        List<CourseEnrichment> approved = enrichmentRepository.findByStatus(EnrichmentStatus.APPROVED);

        Map<String, Long> toolFrequency = approved.stream()
                .flatMap(e -> e.getTools().stream())
                .collect(Collectors.groupingBy(
                        Tool::getNameEn,
                        Collectors.counting()
                ));

        return toolFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> new ChartDataDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
