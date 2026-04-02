package com.muqarariplus.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_addition_requests")
@Getter @Setter
public class CourseAdditionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_name_ar", nullable = false)
    private String courseNameAr;

    @Column(name = "course_name_en", nullable = false)
    private String courseNameEn;

    @Column(name = "course_code")
    private String courseCode;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseAdditionRequestStatus status = CourseAdditionRequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
}
