package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.service.CourseEnrichmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/enrichments")
public class AdminEnrichmentController {

    private final CourseEnrichmentService enrichmentService;

    public AdminEnrichmentController(CourseEnrichmentService enrichmentService) {
        this.enrichmentService = enrichmentService;
    }

    /**
     * GET /admin/enrichments/pending — Displays the pending enrichment moderation queue.
     */
    @GetMapping("/pending")
    public String pendingEnrichments(Model model) {
        List<CourseEnrichment> pendingEnrichments = enrichmentService.getPendingEnrichments();
        model.addAttribute("pendingEnrichments", pendingEnrichments);
        return "admin/pending-enrichments";
    }

    /**
     * POST /admin/enrichments/{id}/approve — Approves a pending enrichment.
     */
    @PostMapping("/{id}/approve")
    public String approveEnrichment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            enrichmentService.approveEnrichment(id);
            redirectAttributes.addFlashAttribute("successMsg", "Enrichment approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to approve: " + e.getMessage());
        }
        return "redirect:/admin/enrichments/pending";
    }

    /**
     * POST /admin/enrichments/{id}/reject — Rejects a pending enrichment.
     */
    @PostMapping("/{id}/reject")
    public String rejectEnrichment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            enrichmentService.rejectEnrichment(id);
            redirectAttributes.addFlashAttribute("successMsg", "Enrichment rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to reject: " + e.getMessage());
        }
        return "redirect:/admin/enrichments/pending";
    }
}
