package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import com.muqarariplus.platform.service.ExpertService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrichmentRepository enrichmentRepository;
    private final ExpertService expertService;

    public AdminController(UserRepository userRepository,
                           CourseRepository courseRepository,
                           CourseEnrichmentRepository enrichmentRepository,
                           ExpertService expertService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrichmentRepository = enrichmentRepository;
        this.expertService = expertService;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("totalStudents", userRepository.countByRole("ROLE_STUDENT"));
        model.addAttribute("verifiedExperts", expertService.getApprovedExperts().size());
        model.addAttribute("coursesEnriched", enrichmentRepository.count());

        // Get pending experts from Expert entity (with CV/LinkedIn data)
        List<Expert> pendingExperts = expertService.getPendingExperts();
        model.addAttribute("pendingExperts", pendingExperts);
        model.addAttribute("pendingExpertsCount", pendingExperts.size());

        model.addAttribute("allUsers", userRepository.findAll());

        return "admin";
    }

    @PostMapping("/admin/expert/approve/{id}")
    public String approveExpert(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            expertService.approveExpert(id);
            redirectAttributes.addFlashAttribute("successMsg", "Expert approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to approve expert: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/expert/reject/{id}")
    public String rejectExpert(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            expertService.rejectExpert(id);
            redirectAttributes.addFlashAttribute("successMsg", "Expert rejected. Cooldown activated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to reject expert: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // ════════════════════════════════════════════════════════════════
    // Legacy endpoints kept for backward compatibility — redirect to new ones
    // ════════════════════════════════════════════════════════════════

    @PostMapping("/admin/approve-expert")
    public String legacyApproveExpert(@RequestParam Long expertId, RedirectAttributes redirectAttributes) {
        return approveExpert(expertId, redirectAttributes);
    }

    @PostMapping("/admin/reject-expert")
    public String legacyRejectExpert(@RequestParam Long expertId, RedirectAttributes redirectAttributes) {
        return rejectExpert(expertId, redirectAttributes);
    }
}
