package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByNameArContainingIgnoreCaseOrNameEnContainingIgnoreCase(String nameAr, String nameEn);
}
