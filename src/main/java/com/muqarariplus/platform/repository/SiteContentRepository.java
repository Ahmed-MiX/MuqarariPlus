package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.SiteContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteContentRepository extends JpaRepository<SiteContent, Long> {
}
