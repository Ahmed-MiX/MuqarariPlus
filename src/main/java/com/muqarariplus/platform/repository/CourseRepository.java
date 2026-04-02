package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    List<Course> findByNameArContainingIgnoreCaseOrNameEnContainingIgnoreCase(String nameAr, String nameEn);

    /**
     * Global Smart Search: finds courses where the keyword matches
     * the code, nameAr, or nameEn (case-insensitive).
     */
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.nameAr) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchByKeyword(@Param("keyword") String keyword);

    long countByMajorId(Long majorId);
}
