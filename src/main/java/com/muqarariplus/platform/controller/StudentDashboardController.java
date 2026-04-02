package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    private final StudentDashboardService dashboardService;
    private final CourseEnrichmentRepository enrichmentRepository;
    private final EngagementService engagementService;
    private final UniversityService universityService;
    private final CollegeService collegeService;
    private final MajorService majorService;
    private final CourseService courseService;

    public StudentDashboardController(StudentDashboardService dashboardService,
                                      CourseEnrichmentRepository enrichmentRepository,
                                      EngagementService engagementService,
                                      UniversityService universityService,
                                      CollegeService collegeService,
                                      MajorService majorService,
                                      CourseService courseService) {
        this.dashboardService = dashboardService;
        this.enrichmentRepository = enrichmentRepository;
        this.engagementService = engagementService;
        this.universityService = universityService;
        this.collegeService = collegeService;
        this.majorService = majorService;
        this.courseService = courseService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        String identifier = principal.getName();

        Set<Course> enrolledCourses = dashboardService.getEnrolledCourses(identifier);
        Set<Skill> acquiredSkills = dashboardService.getAcquiredSkills(identifier);
        Set<Tool> acquiredTools = dashboardService.getAcquiredTools(identifier);
        Set<com.muqarariplus.platform.entity.ProfessionalCertificate> acquiredCerts = dashboardService.getAcquiredCertificates(identifier);
        List<CourseEnrichment> savedInsights = engagementService.getBookmarkedEnrichments(identifier);
        List<University> universities = universityService.getAllUniversities();

        Set<Long> enrolledCourseIds = enrolledCourses.stream()
                .map(Course::getId)
                .collect(Collectors.toSet());

        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        model.addAttribute("acquiredSkills", acquiredSkills);
        model.addAttribute("acquiredTools", acquiredTools);
        model.addAttribute("acquiredCerts", acquiredCerts);
        model.addAttribute("universities", universities);
        model.addAttribute("skillCount", acquiredSkills.size());
        model.addAttribute("toolCount", acquiredTools.size());
        model.addAttribute("certCount", acquiredCerts.size());
        model.addAttribute("savedInsights", savedInsights);
        model.addAttribute("savedCount", savedInsights.size());

        return "student/dashboard";
    }

    @PostMapping("/enroll")
    public String enroll(@RequestParam("courseId") Long courseId,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        try {
            dashboardService.enrollStudentInCourse(principal.getName(), courseId);
            redirectAttributes.addFlashAttribute("successMsg", "course_enrolled");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    @PostMapping("/unenroll")
    public String unenroll(@RequestParam("courseId") Long courseId,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            dashboardService.removeStudentFromCourse(principal.getName(), courseId);
            redirectAttributes.addFlashAttribute("successMsg", "course_unenrolled");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    @GetMapping("/api/colleges")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getColleges(@RequestParam Long universityId) {
        try {
            var colleges = collegeService.getCollegesByUniversityId(universityId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (var college : colleges) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", college.getId());
                map.put("nameAr", college.getNameAr());
                map.put("nameEn", college.getNameEn());
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/api/majors")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getMajors(@RequestParam Long collegeId) {
        try {
            var majors = majorService.getMajorsByCollegeId(collegeId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (var major : majors) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", major.getId());
                map.put("nameAr", major.getNameAr());
                map.put("nameEn", major.getNameEn());
                map.put("code", major.getCode());
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/api/courses")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getCourses(
            @RequestParam Long majorId,
            @RequestParam(required = false) String search) {
        try {
            var courses = courseService.getCoursesByMajorId(majorId);
            if (search != null && !search.isBlank()) {
                String keyword = search.toLowerCase();
                courses = courses.stream()
                        .filter(c -> (c.getCode() != null && c.getCode().toLowerCase().contains(keyword))
                                || c.getNameAr().toLowerCase().contains(keyword)
                                || c.getNameEn().toLowerCase().contains(keyword))
                        .toList();
            }
            List<Map<String, Object>> result = new ArrayList<>();
            for (var course : courses) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", course.getId());
                map.put("code", course.getCode());
                map.put("nameAr", course.getNameAr());
                map.put("nameEn", course.getNameEn());
                map.put("enrichmentCount", enrichmentRepository.countByCourseId(course.getId()));
                map.put("majorNameAr", course.getMajor().getNameAr());
                map.put("majorNameEn", course.getMajor().getNameEn());
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of(Map.of("error", e.getMessage())));
        }
    }
}
