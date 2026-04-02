package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.dto.TechCvDTO;
import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.StudentEnrollment;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.StudentEnrollmentRepository;
import com.muqarariplus.platform.service.StudentNexusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════════
 * STUDENT CONTROLLER — The Student Nexus & Tech CV Generator
 * Provides the student enrollment dashboard and dynamic Tech CV
 * generation page. Secured with @PreAuthorize ROLE_STUDENT RBAC.
 * Uses /student/nexus prefix to coexist with StudentDashboardController.
 * ═══════════════════════════════════════════════════════════════════
 */
@Controller
@RequestMapping("/student/nexus")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentController {

    private final StudentNexusService nexusService;
    private final CourseRepository courseRepository;
    private final StudentEnrollmentRepository enrollmentRepository;

    // ═══════════════════════════════════════════════════════════════
    // GET /student/nexus/dashboard — Show enrollment dashboard
    // Injects all available courses and current enrollments.
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        String email = principal.getName();

        // All available courses in the platform
        List<Course> allCourses = courseRepository.findAll();

        // Current student enrollments (via StudentEnrollment entity)
        List<StudentEnrollment> enrollments = enrollmentRepository.findByUserEmail(email);

        // Build a set of already-enrolled course IDs for UI toggle
        Set<Long> enrolledCourseIds = enrollments.stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());

        model.addAttribute("courses", allCourses);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        model.addAttribute("enrollmentCount", enrollments.size());

        return "student/dashboard";
    }

    // ═══════════════════════════════════════════════════════════════
    // POST /student/nexus/enroll — Enroll student in a course
    // Delegates to StudentNexusService.enroll(), redirects back.
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/enroll")
    public String enroll(@RequestParam("courseId") Long courseId,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        try {
            nexusService.enroll(principal.getName(), courseId);
            redirectAttributes.addFlashAttribute("successMsg",
                    "✅ تم التسجيل في المقرر بنجاح! — Successfully enrolled in this course!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "❌ حدث خطأ غير متوقع — An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/student/nexus/dashboard";
    }

    // ═══════════════════════════════════════════════════════════════
    // GET /student/nexus/cv — Generate and display the Tech CV
    // Aggregates all Tools & Certifications from APPROVED enrichments.
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/cv")
    public String techCv(Principal principal, Model model) {
        String email = principal.getName();
        TechCvDTO techCv = nexusService.generateTechCv(email);
        model.addAttribute("techCv", techCv);
        return "student/tech-cv";
    }
}
