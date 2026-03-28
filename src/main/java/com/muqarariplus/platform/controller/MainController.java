package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class MainController {

    private final CourseRepository courseRepository;
    private final CourseEnrichmentRepository enrichmentRepository;

    public MainController(CourseRepository courseRepository, CourseEnrichmentRepository enrichmentRepository) {
        this.courseRepository = courseRepository;
        this.enrichmentRepository = enrichmentRepository;
    }



    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin";
    }

    @GetMapping("/student-dashboard")
    public String studentDashboard() {
        return "student-dashboard";
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/courses")
    public String courses(@RequestParam(required = false) String search, Model model) {
        List<Course> courses;
        if (search != null && !search.isEmpty()) {
            courses = courseRepository.findByNameArContainingIgnoreCaseOrNameEnContainingIgnoreCase(search, search);
        } else {
            courses = courseRepository.findAll();
        }
        
        model.addAttribute("courses", courses);
        return "courses";
    }

    @GetMapping("/course/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) {
             return "redirect:/courses";
        }
        
        Course course = courseOpt.get();
        List<CourseEnrichment> enrichments = enrichmentRepository.findByCourseIdAndVerificationStatus(id, "APPROVED");
        
        model.addAttribute("course", course);
        model.addAttribute("enrichments", enrichments);
        return "course-detail";
    }
}
