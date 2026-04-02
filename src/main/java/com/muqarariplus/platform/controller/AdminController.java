package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.College;
import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.repository.*;
import com.muqarariplus.platform.service.ExpertService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrichmentRepository enrichmentRepository;
    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final ExpertService expertService;

    public AdminController(UserRepository userRepository,
                           CourseRepository courseRepository,
                           CourseEnrichmentRepository enrichmentRepository,
                           CollegeRepository collegeRepository,
                           MajorRepository majorRepository,
                           ExpertService expertService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrichmentRepository = enrichmentRepository;
        this.collegeRepository = collegeRepository;
        this.majorRepository = majorRepository;
        this.expertService = expertService;
    }

    @GetMapping("")
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

    @PostMapping("/expert/approve/{id}")
    public String approveExpert(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            expertService.approveExpert(id);
            redirectAttributes.addFlashAttribute("successMsg", "Expert approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to approve expert: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/expert/reject/{id}")
    public String rejectExpert(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            expertService.rejectExpert(id);
            redirectAttributes.addFlashAttribute("successMsg", "Expert rejected. Cooldown activated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to reject expert: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/academic")
    public String academicDashboard(Model model) {
        model.addAttribute("universities", collegeRepository.findAll().stream()
                .map(College::getUniversity).distinct().toList());
        model.addAttribute("colleges", collegeRepository.findAll());
        model.addAttribute("majors", majorRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/academic";
    }

    // ════════════════════════════════════════════════════════════════
    // Legacy endpoints kept for backward compatibility — redirect to new ones
    // ════════════════════════════════════════════════════════════════

    @PostMapping("/approve-expert")
    public String legacyApproveExpert(@RequestParam Long expertId, RedirectAttributes redirectAttributes) {
        return approveExpert(expertId, redirectAttributes);
    }

    @PostMapping("/reject-expert")
    public String legacyRejectExpert(@RequestParam Long expertId, RedirectAttributes redirectAttributes) {
        return rejectExpert(expertId, redirectAttributes);
    }
}
