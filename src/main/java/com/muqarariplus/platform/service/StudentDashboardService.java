package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrichmentRepository enrichmentRepository;

    public StudentDashboardService(UserRepository userRepository,
                                   CourseRepository courseRepository,
                                   CourseEnrichmentRepository enrichmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrichmentRepository = enrichmentRepository;
    }

    /**
     * Resolves the User from a login identifier (email or username).
     */
    private User resolveUser(String identifier) {
        User user = userRepository.findByEmail(identifier);
        if (user == null) {
            user = userRepository.findByUsername(identifier);
        }
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + identifier);
        }
        return user;
    }

    /**
     * Enrolls a student in a course.
     */
    @Transactional
    public void enrollStudentInCourse(String identifier, Long courseId) {
        User user = resolveUser(identifier);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        user.addCourse(course);
        userRepository.save(user);
    }

    /**
     * Removes a student from a course.
     */
    @Transactional
    public void removeStudentFromCourse(String identifier, Long courseId) {
        User user = resolveUser(identifier);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        user.removeCourse(course);
        userRepository.save(user);
    }

    /**
     * Returns the user's enrolled courses.
     */
    @Transactional(readOnly = true)
    public Set<Course> getEnrolledCourses(String identifier) {
        User user = resolveUser(identifier);
        // Force initialize the lazy collection
        user.getEnrolledCourses().size();
        return user.getEnrolledCourses();
    }

    /**
     * THE MAGIC ENGINE: Extracts all unique Skills from APPROVED enrichments
     * across all of the student's enrolled courses.
     */
    @Transactional(readOnly = true)
    public Set<Skill> getAcquiredSkills(String identifier) {
        User user = resolveUser(identifier);
        Set<Skill> skills = new LinkedHashSet<>();

        for (Course course : user.getEnrolledCourses()) {
            List<CourseEnrichment> enrichments = enrichmentRepository
                    .findByCourseIdAndStatus(course.getId(), EnrichmentStatus.APPROVED);
            for (CourseEnrichment enrichment : enrichments) {
                skills.addAll(enrichment.getSkills());
            }
        }
        return skills;
    }

    /**
     * THE MAGIC ENGINE: Extracts all unique Tools from APPROVED enrichments
     * across all of the student's enrolled courses.
     */
    @Transactional(readOnly = true)
    public Set<Tool> getAcquiredTools(String identifier) {
        User user = resolveUser(identifier);
        Set<Tool> tools = new LinkedHashSet<>();

        for (Course course : user.getEnrolledCourses()) {
            List<CourseEnrichment> enrichments = enrichmentRepository
                    .findByCourseIdAndStatus(course.getId(), EnrichmentStatus.APPROVED);
            for (CourseEnrichment enrichment : enrichments) {
                tools.addAll(enrichment.getTools());
            }
        }
        return tools;
    }
}
