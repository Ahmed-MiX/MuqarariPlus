package com.muqarariplus.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "professional_certificates")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nameEn;

    @Column(nullable = false)
    private String nameAr;

    @Column(nullable = false)
    private String issuingBody;
}
