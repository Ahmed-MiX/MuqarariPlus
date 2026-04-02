package com.muqarariplus.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "experts")
@Getter @Setter
public class Expert {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bioAr;

    @Column(columnDefinition = "TEXT")
    private String bioEn;

    private String cvUrl;
    private String cvFilePath;
    private String linkedinUrl;
    private String githubUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpertStatus status = ExpertStatus.NONE;

    private LocalDateTime lastSubmissionTime;

    @Column
    private Double rating = 0.0;

    @OneToMany(mappedBy = "expert")
    private List<CourseEnrichment> enrichments;
}
