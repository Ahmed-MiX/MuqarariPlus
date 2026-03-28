package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrichmentRepository enrichmentRepository;

    public AdminController(UserRepository userRepository, CourseRepository courseRepository, CourseEnrichmentRepository enrichmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrichmentRepository = enrichmentRepository;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("totalStudents", userRepository.countByRole("ROLE_STUDENT"));
        model.addAttribute("verifiedExperts", userRepository.countByRoleAndStatus("ROLE_EXPERT", "APPROVED"));
        model.addAttribute("pendingExpertsCount", userRepository.countByRoleAndStatus("ROLE_EXPERT", "PENDING"));
        model.addAttribute("coursesEnriched", enrichmentRepository.count()); // Simplistic aggregate metric

        model.addAttribute("pendingExperts", userRepository.findByRoleAndStatus("ROLE_EXPERT", "PENDING"));
        model.addAttribute("allUsers", userRepository.findAll());

        return "admin";
    }

    @PostMapping("/admin/approve-expert")
    public String approveExpert(@RequestParam Long expertId) {
        User expert = userRepository.findById(expertId).orElse(null);
        if (expert != null && "ROLE_EXPERT".equals(expert.getRole())) {
            expert.setStatus("APPROVED");
            userRepository.save(expert);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/reject-expert")
    public String rejectExpert(@RequestParam Long expertId) {
        User expert = userRepository.findById(expertId).orElse(null);
        if (expert != null && "ROLE_EXPERT".equals(expert.getRole())) {
            userRepository.delete(expert); // Full wipe or flag status as REJECTED. Choosing wipe to match strict requirements.
        }
        return "redirect:/admin";
    }
}
