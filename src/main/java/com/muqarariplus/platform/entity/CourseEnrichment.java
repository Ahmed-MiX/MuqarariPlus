package com.muqarariplus.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_enrichments")
@Getter @Setter
public class CourseEnrichment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

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

    @Column(name = "verification_status")
    private String verificationStatus = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
