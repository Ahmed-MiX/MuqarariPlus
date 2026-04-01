package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.entity.Skill;
import com.muqarariplus.platform.entity.Tool;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.service.EngagementService;
import com.muqarariplus.platform.service.StudentDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    private final StudentDashboardService dashboardService;
    private final CourseRepository courseRepository;
    private final EngagementService engagementService;

    public StudentDashboardController(StudentDashboardService dashboardService,
                                     CourseRepository courseRepository,
                                     EngagementService engagementService) {
        this.dashboardService = dashboardService;
        this.courseRepository = courseRepository;
        this.engagementService = engagementService;
    }

    /**
     * GET /student/dashboard — Main student dashboard with enrolled courses,
     * acquired skills, acquired tools, and saved insights.
     */
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        String identifier = principal.getName();

        // Enrolled courses
        Set<Course> enrolledCourses = dashboardService.getEnrolledCourses(identifier);

        // The Magic: dynamically acquired Skills & Tools & Certs
        Set<Skill> acquiredSkills = dashboardService.getAcquiredSkills(identifier);
        Set<Tool> acquiredTools = dashboardService.getAcquiredTools(identifier);
        Set<com.muqarariplus.platform.entity.ProfessionalCertificate> acquiredCerts = dashboardService.getAcquiredCertificates(identifier);

        // All available courses (for the enrollment dropdown)
        List<Course> allCourses = courseRepository.findAll();

        // Bookmarked / Saved Enrichments
        List<CourseEnrichment> savedInsights = engagementService.getBookmarkedEnrichments(identifier);

        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("acquiredSkills", acquiredSkills);
        model.addAttribute("acquiredTools", acquiredTools);
        model.addAttribute("acquiredCerts", acquiredCerts);
        model.addAttribute("allCourses", allCourses);
        model.addAttribute("skillCount", acquiredSkills.size());
        model.addAttribute("toolCount", acquiredTools.size());
        model.addAttribute("certCount", acquiredCerts.size());
        model.addAttribute("savedInsights", savedInsights);
        model.addAttribute("savedCount", savedInsights.size());

        return "student/dashboard";
    }

    /**
     * POST /student/enroll — Enroll the student in a course.
     */
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

    /**
     * POST /student/unenroll — Remove the student from a course.
     */
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
}
