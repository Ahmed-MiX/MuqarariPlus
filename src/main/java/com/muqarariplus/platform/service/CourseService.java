package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.Major;
import com.muqarariplus.platform.payload.requests.CourseRequest;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.MajorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final MajorRepository majorRepository;
    private final CourseEnrichmentRepository enrichmentRepository;

    public CourseService(CourseRepository courseRepository, MajorRepository majorRepository, CourseEnrichmentRepository enrichmentRepository) {
        this.courseRepository = courseRepository;
        this.majorRepository = majorRepository;
        this.enrichmentRepository = enrichmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByMajorId(Long majorId) {
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new IllegalArgumentException("Major not found with ID: " + majorId));
        return major.getCourses();
    }

    @Transactional
    public Course createCourse(CourseRequest request) {
        Major major = majorRepository.findById(request.majorId())
                .orElseThrow(() -> new IllegalArgumentException("Major not found with ID: " + request.majorId()));

        Course course = new Course();
        course.setCode(request.code());
        course.setNameEn(request.nameEn());
        course.setNameAr(request.nameAr());
        course.setDescriptionEn(request.descriptionEn());
        course.setDescriptionAr(request.descriptionAr());
        course.setSyllabusUrl(request.syllabusUrl());
        course.setMajor(major);
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, CourseRequest request) {
        Course course = getCourseById(id);
        course.setCode(request.code());
        course.setNameEn(request.nameEn());
        course.setNameAr(request.nameAr());
        course.setDescriptionEn(request.descriptionEn());
        course.setDescriptionAr(request.descriptionAr());
        course.setSyllabusUrl(request.syllabusUrl());

        if (!course.getMajor().getId().equals(request.majorId())) {
            Major newMajor = majorRepository.findById(request.majorId())
                    .orElseThrow(() -> new IllegalArgumentException("Major not found with ID: " + request.majorId()));
            course.setMajor(newMajor);
        }
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        long enrichmentCount = enrichmentRepository.countByCourseId(id);
        if (enrichmentCount > 0) {
            throw new IllegalStateException(
                "Cannot delete course '" + course.getNameEn() + "' — it has " + enrichmentCount + " enrichment(s) linked to it.");
        }
        courseRepository.delete(course);
    }
}
