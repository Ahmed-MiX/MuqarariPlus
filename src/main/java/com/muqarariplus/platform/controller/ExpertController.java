package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.entity.ExpertStatus;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.service.CourseEnrichmentService;
import com.muqarariplus.platform.service.ExpertService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ExpertController {

    private final ExpertService expertService;
    private final UserRepository userRepository;
    private final CourseEnrichmentService enrichmentService;

    public ExpertController(ExpertService expertService,
                            UserRepository userRepository,
                            CourseEnrichmentService enrichmentService) {
        this.expertService = expertService;
        this.userRepository = userRepository;
        this.enrichmentService = enrichmentService;
    }

    @GetMapping("/expert")
    public String portalDashboard(Model model, Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        // Lazy creation: create Expert entity on first visit if it doesn't exist
        Expert expert = expertService.getOrCreateExpert(user);

        // Calculate cooldown for REJECTED status
        long cooldownSeconds = expertService.getCooldownRemainingSeconds(expert);

        // Determine if the form should be shown
        boolean showVerificationForm = (expert.getStatus() == ExpertStatus.NONE)
                || (expert.getStatus() == ExpertStatus.REJECTED && cooldownSeconds <= 0);

        boolean showCooldownTimer = (expert.getStatus() == ExpertStatus.REJECTED && cooldownSeconds > 0);
        boolean isApproved = expert.getStatus() == ExpertStatus.APPROVED;

        model.addAttribute("expert", expert);
        model.addAttribute("expertStatus", expert.getStatus().name());
        model.addAttribute("cooldownSeconds", cooldownSeconds);
        model.addAttribute("showVerificationForm", showVerificationForm);
        model.addAttribute("showCooldownTimer", showCooldownTimer);
        model.addAttribute("isApproved", isApproved);

        // ── Expert Impact Metrics (for APPROVED experts) ────────────────
        if (isApproved) {
            String identifier = authentication.getName();
            List<CourseEnrichment> myEnrichments = enrichmentService.getAllEnrichmentsByExpert(identifier);
            long approvedCount = enrichmentService.countApprovedEnrichmentsByExpert(identifier);
            long pendingCount = enrichmentService.countPendingEnrichmentsByExpert(identifier);
            long studentImpact = enrichmentService.calculateTotalStudentImpact(identifier);

            model.addAttribute("myEnrichments", myEnrichments);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("studentImpact", studentImpact);
        }

        return "expert/dashboard";
    }

    @PostMapping("/expert/verify")
    public String submitVerification(
            @RequestParam("cvFile") MultipartFile cvFile,
            @RequestParam("linkedinUrl") String linkedinUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = resolveUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            expertService.submitVerification(user, cvFile, linkedinUrl);
            redirectAttributes.addFlashAttribute("successMsg", "verification_submitted");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/expert";
    }

    /**
     * Resolves the User entity from the Spring Security Authentication object.
     * Supports both email-based and username-based login identifiers.
     */
    private User resolveUser(Authentication authentication) {
        if (authentication == null) return null;
        String identifier = authentication.getName();
        User user = userRepository.findByEmail(identifier);
        if (user == null) {
            user = userRepository.findByUsername(identifier);
        }
        return user;
    }
}
