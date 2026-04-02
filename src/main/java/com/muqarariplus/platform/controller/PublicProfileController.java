package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.*;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.repository.ExpertRepository;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.service.StudentDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

/**
 * ═══════════════════════════════════════════════════════════════════
 * PUBLIC PROFILE CONTROLLER — The Dual-Faced Identity Engine
 * Public Student Tech CVs and Expert Authority Pages.
 * These endpoints are PUBLICLY accessible (no auth required).
 * ═══════════════════════════════════════════════════════════════════
 */
@Controller
public class PublicProfileController {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;
    private final CourseEnrichmentRepository enrichmentRepository;
    private final StudentDashboardService studentDashboardService;

    public PublicProfileController(UserRepository userRepository,
                                   ExpertRepository expertRepository,
                                   CourseEnrichmentRepository enrichmentRepository,
                                   StudentDashboardService studentDashboardService) {
        this.userRepository = userRepository;
        this.expertRepository = expertRepository;
        this.enrichmentRepository = enrichmentRepository;
        this.studentDashboardService = studentDashboardService;
    }

    // ═══════════════════════════════════════════════════════════════
    // PUBLIC STUDENT TECH CV — /u/{username}
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/u/{username}")
    @Transactional(readOnly = true)
    public String publicStudentProfile(@PathVariable String username, Model model) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "error";
        }

        // Use email as identifier for the dashboard service (it resolves both)
        String identifier = user.getEmail();

        // Enrolled courses
        Set<Course> enrolledCourses = studentDashboardService.getEnrolledCourses(identifier);

        // The Omni-Graph: dynamically acquired Skills, Tools & Certs
        Set<Skill> acquiredSkills = studentDashboardService.getAcquiredSkills(identifier);
        Set<Tool> acquiredTools = studentDashboardService.getAcquiredTools(identifier);
        Set<ProfessionalCertificate> acquiredCerts = studentDashboardService.getAcquiredCertificates(identifier);

        model.addAttribute("profileUser", user);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("acquiredSkills", acquiredSkills);
        model.addAttribute("acquiredTools", acquiredTools);
        model.addAttribute("acquiredCerts", acquiredCerts);
        model.addAttribute("skillCount", acquiredSkills.size());
        model.addAttribute("toolCount", acquiredTools.size());
        model.addAttribute("certCount", acquiredCerts.size());
        model.addAttribute("courseCount", enrolledCourses.size());

        return "profile/public-student";
    }

    // ═══════════════════════════════════════════════════════════════
    // PUBLIC EXPERT AUTHORITY PAGE — /expert-profile/{username}
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/expert-profile/{username}")
    @Transactional(readOnly = true)
    public String publicExpertProfile(@PathVariable String username, Model model) {
        // Find the expert via user username
        Optional<Expert> expertOpt = expertRepository.findByUserUsername(username);
        if (expertOpt.isEmpty()) {
            return "error";
        }

        Expert expert = expertOpt.get();
        User user = expert.getUser();

        // Get all APPROVED enrichments by this expert
        List<CourseEnrichment> enrichments = enrichmentRepository.findByExpertUserId(user.getId());
        List<CourseEnrichment> approvedEnrichments = new ArrayList<>();
        Set<Skill> expertSkills = new LinkedHashSet<>();
        Set<Tool> expertTools = new LinkedHashSet<>();
        Set<ProfessionalCertificate> expertCerts = new LinkedHashSet<>();
        Set<Course> enrichedCourses = new LinkedHashSet<>();

        for (CourseEnrichment e : enrichments) {
            if (e.getStatus() == EnrichmentStatus.APPROVED) {
                approvedEnrichments.add(e);
                expertSkills.addAll(e.getSkills());
                expertTools.addAll(e.getTools());
                expertCerts.addAll(e.getCertificates());
                enrichedCourses.add(e.getCourse());
            }
        }

        // Calculate Impact Score: count of students enrolled in courses this expert enriched
        long impactScore = 0;
        for (Course course : enrichedCourses) {
            // Count users enrolled in this course
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                if (u.getEnrolledCourses().contains(course)) {
                    impactScore++;
                }
            }
        }

        model.addAttribute("expert", expert);
        model.addAttribute("profileUser", user);
        model.addAttribute("approvedEnrichments", approvedEnrichments);
        model.addAttribute("enrichmentCount", approvedEnrichments.size());
        model.addAttribute("expertSkills", expertSkills);
        model.addAttribute("expertTools", expertTools);
        model.addAttribute("expertCerts", expertCerts);
        model.addAttribute("enrichedCourses", enrichedCourses);
        model.addAttribute("impactScore", impactScore);
        model.addAttribute("courseCount", enrichedCourses.size());

        return "profile/public-expert";
    }
}
