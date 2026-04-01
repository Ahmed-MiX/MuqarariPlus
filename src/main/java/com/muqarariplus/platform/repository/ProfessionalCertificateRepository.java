package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.ProfessionalCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProfessionalCertificateRepository extends JpaRepository<ProfessionalCertificate, Long> {
    Optional<ProfessionalCertificate> findByNameEn(String nameEn);
}
