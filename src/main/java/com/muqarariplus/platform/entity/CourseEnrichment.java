package com.muqarariplus.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "course_enrichments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrichment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // ── Knowledge Graph: unified rich content field ──────────────────────
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // ── Knowledge Graph: ManyToMany relationships ───────────────────────
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "enrichment_skills",
        joinColumns = @JoinColumn(name = "enrichment_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "enrichment_tools",
        joinColumns = @JoinColumn(name = "enrichment_id"),
        inverseJoinColumns = @JoinColumn(name = "tool_id")
    )
    private Set<Tool> tools = new HashSet<>();

    // ── Status ──────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private EnrichmentStatus status = EnrichmentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Legacy pillar fields (retained for backward compatibility) ──────
    @Column(columnDefinition = "TEXT")
    private String applicationAr;

    @Column(columnDefinition = "TEXT")
    private String applicationEn;

    @Column(columnDefinition = "TEXT")
    private String roadmapAr;

    @Column(columnDefinition = "TEXT")
    private String roadmapEn;

    @Column(columnDefinition = "TEXT")
    private String resourcesAr;

    @Column(columnDefinition = "TEXT")
    private String resourcesEn;
}
