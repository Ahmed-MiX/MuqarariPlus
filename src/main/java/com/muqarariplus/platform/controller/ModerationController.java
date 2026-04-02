package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.service.ModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * MODERATION CONTROLLER — The Bridge Command Center
 * Provides the admin moderation dashboard for reviewing, approving,
 * and rejecting expert-submitted CourseEnrichments. Secured with
 * dual-role @PreAuthorize for ADMIN and SUPER_ADMIN access.
 * ═══════════════════════════════════════════════════════════════════
 */
@Controller
@RequestMapping("/admin/moderation")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    // ═══════════════════════════════════════════════════════════════
    // GET /admin/moderation/dashboard — The Moderation Queue View
    // Injects all PENDING enrichments into the Thymeleaf model.
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/dashboard")
    public String moderationDashboard(Model model) {
        List<CourseEnrichment> pendingEnrichments = moderationService.getPendingEnrichments();
        model.addAttribute("pendingEnrichments", pendingEnrichments);
        model.addAttribute("pendingCount", pendingEnrichments.size());
        return "admin/moderation-dashboard";
    }

    // ═══════════════════════════════════════════════════════════════
    // POST /admin/moderation/approve/{id} — Green Light Protocol
    // Approves an enrichment and redirects back with success flash.
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/approve/{id}")
    public String approveEnrichment(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            moderationService.approveEnrichment(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    "✅ تم اعتماد الإثراء بنجاح، وسيظهر الآن للطلاب — Enrichment approved successfully and is now visible to students.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "❌ حدث خطأ أثناء اعتماد الإثراء — An error occurred while approving: " + e.getMessage());
        }
        return "redirect:/admin/moderation/dashboard";
    }

    // ═══════════════════════════════════════════════════════════════
    // POST /admin/moderation/reject/{id} — Red Flag Protocol
    // Rejects an enrichment and redirects back with success flash.
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/reject/{id}")
    public String rejectEnrichment(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            moderationService.rejectEnrichment(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    "🚫 تم رفض الإثراء — Enrichment has been rejected.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "❌ حدث خطأ أثناء رفض الإثراء — An error occurred while rejecting: " + e.getMessage());
        }
        return "redirect:/admin/moderation/dashboard";
    }
}
