package com.muqarariplus.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "site_content")
@Getter
@Setter
public class SiteContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_key", nullable = false, unique = true, length = 150)
    private String contentKey;

    @Column(name = "value_en", columnDefinition = "TEXT", nullable = false)
    private String valueEn;

    @Column(name = "value_ar", columnDefinition = "TEXT", nullable = false)
    private String valueAr;
}
