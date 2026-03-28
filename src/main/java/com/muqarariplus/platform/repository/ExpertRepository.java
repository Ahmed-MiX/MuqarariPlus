package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpertRepository extends JpaRepository<Expert, Long> {
    List<Expert> findByIsVerifiedTrue();
}
