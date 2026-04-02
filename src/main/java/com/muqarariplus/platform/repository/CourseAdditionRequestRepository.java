package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.CourseAdditionRequest;
import com.muqarariplus.platform.entity.CourseAdditionRequestStatus;
import com.muqarariplus.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseAdditionRequestRepository extends JpaRepository<CourseAdditionRequest, Long> {

    List<CourseAdditionRequest> findByStatus(CourseAdditionRequestStatus status);

    List<CourseAdditionRequest> findByRequestedBy(User user);

    List<CourseAdditionRequest> findByMajorId(Long majorId);

    long countByStatus(CourseAdditionRequestStatus status);

    long countByMajorId(Long majorId);
}
