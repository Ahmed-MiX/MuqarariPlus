package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.service.CourseEnrichmentService;
import com.muqarariplus.platform.service.UniversityService;
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
    private final CourseEnrichmentService enrichmentService;
    private final UniversityService universityService;

    public MainController(CourseRepository courseRepository,
                          CourseEnrichmentService enrichmentService,
                          UniversityService universityService) {
        this.courseRepository = courseRepository;
        this.enrichmentService = enrichmentService;
        this.universityService = universityService;
    }

    @GetMapping("/student-dashboard")
    public String studentDashboardLegacy() {
        return "redirect:/student/dashboard";
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/courses")
    public String courses(@RequestParam(required = false) String search,
                          @RequestParam(required = false) Long universityId,
                          @RequestParam(required = false) Long collegeId,
                          @RequestParam(required = false) Long majorId,
                          Model model) {
        model.addAttribute("universities", universityService.getAllUniversities());

        List<Course> courses;
        if (search != null && !search.isEmpty()) {
            courses = courseRepository.findByNameArContainingIgnoreCaseOrNameEnContainingIgnoreCase(search, search);
        } else if (majorId != null) {
            courses = courseRepository.findAll().stream()
                    .filter(c -> c.getMajor() != null && c.getMajor().getId().equals(majorId))
                    .toList();
        } else if (collegeId != null) {
            courses = courseRepository.findAll().stream()
                    .filter(c -> c.getMajor() != null && c.getMajor().getCollege() != null && c.getMajor().getCollege().getId().equals(collegeId))
                    .toList();
        } else if (universityId != null) {
            courses = courseRepository.findAll().stream()
                    .filter(c -> c.getMajor() != null && c.getMajor().getCollege() != null && c.getMajor().getCollege().getUniversity() != null && c.getMajor().getCollege().getUniversity().getId().equals(universityId))
                    .toList();
        } else {
            courses = List.of();
        }

        // Build a map of courseId -> approved enrichment count (strict APPROVED only)
        java.util.Map<Long, Long> approvedCounts = new java.util.HashMap<>();
        for (Course c : courses) {
            approvedCounts.put(c.getId(),
                enrichmentService.getApprovedCountForCourse(c.getId()));
        }

        model.addAttribute("courses", courses);
        model.addAttribute("approvedCounts", approvedCounts);
        return "courses";
    }

    @GetMapping("/course/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) {
             return "redirect:/courses";
        }

        Course course = courseOpt.get();
        List<CourseEnrichment> enrichments = enrichmentService.getApprovedEnrichmentsForCourse(id);

        model.addAttribute("course", course);
        model.addAttribute("enrichments", enrichments);
        model.addAttribute("enrichmentCount", enrichments.size());
        return "student/course-details";
    }
}
