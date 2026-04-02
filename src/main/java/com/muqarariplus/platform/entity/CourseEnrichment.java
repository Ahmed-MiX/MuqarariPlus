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

    // ── Engagement: Upvotes & Bookmarks ─────────────────────────────────
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "enrichment_upvotes",
        joinColumns = @JoinColumn(name = "enrichment_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> upvotedByUsers = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "enrichment_bookmarks",
        joinColumns = @JoinColumn(name = "enrichment_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> bookmarkedByUsers = new HashSet<>();

    // ── Engagement: Professional Certifications ─────────────────────────
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "enrichment_certificates",
        joinColumns = @JoinColumn(name = "enrichment_id"),
        inverseJoinColumns = @JoinColumn(name = "certificate_id")
    )
    private Set<ProfessionalCertificate> certificates = new HashSet<>();

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

    // ── Engagement Helpers ──────────────────────────────────────────────

    public int getUpvoteCount() {
        return upvotedByUsers != null ? upvotedByUsers.size() : 0;
    }

    public int getBookmarkCount() {
        return bookmarkedByUsers != null ? bookmarkedByUsers.size() : 0;
    }

    public boolean isUpvotedBy(User user) {
        return upvotedByUsers != null && upvotedByUsers.contains(user);
    }

    public boolean isBookmarkedBy(User user) {
        return bookmarkedByUsers != null && bookmarkedByUsers.contains(user);
    }

    public void addUpvote(User user) {
        this.upvotedByUsers.add(user);
    }

    public void removeUpvote(User user) {
        this.upvotedByUsers.remove(user);
    }

    public void addBookmark(User user) {
        this.bookmarkedByUsers.add(user);
    }

    public void removeBookmark(User user) {
        this.bookmarkedByUsers.remove(user);
    }

    // ═══════════════════════════════════════════════════════════════════
    // IRON VAULT: JPA Lifecycle Constraint — @PrePersist & @PreUpdate
    // Physically blocks enrichment persistence on non-technical courses.
    // Keywords mirror DatabaseSeeder.NON_TECH_KEYWORDS exactly.
    // ═══════════════════════════════════════════════════════════════════

    private static final String[] NON_TECH_KEYWORDS = {
        "ثقافة", "سلم", "إسلام", "عرب", "لغة", "مهارات", "قرآن", "تحرير",
        "islamic", "arabic", "english", "communication", "writing", "reading"
    };

    @PrePersist
    @PreUpdate
    private void validateDomainIntegrity() {
        if (course == null) return;
        String nameEn = course.getNameEn() != null ? course.getNameEn() : "";
        String nameAr = course.getNameAr() != null ? course.getNameAr() : "";
        String combined = (nameEn + " " + nameAr).toLowerCase();
        for (String keyword : NON_TECH_KEYWORDS) {
            if (combined.contains(keyword.toLowerCase())) {
                throw new IllegalStateException(
                    "CRITICAL SECURITY VIOLATION: Attempted to inject technical enrichment " +
                    "into a non-technical course [" + course.getCode() + " - " + nameAr + "]. " +
                    "Iron Vault @PrePersist/@PreUpdate constraint activated. Operation BLOCKED."
                );
            }
        }
    }
}
