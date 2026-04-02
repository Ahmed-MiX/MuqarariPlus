package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.entity.ExpertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExpertRepository extends JpaRepository<Expert, Long> {
    List<Expert> findByStatus(ExpertStatus status);
    Optional<Expert> findByUserEmail(String email);
    Optional<Expert> findByUserId(Long userId);
    Optional<Expert> findByUserUsername(String username);
}
