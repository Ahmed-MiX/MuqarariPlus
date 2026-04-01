package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.dto.ChartDataDTO;
import com.muqarariplus.platform.service.AnalyticsService;
import com.muqarariplus.platform.service.CourseEnrichmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final AnalyticsService analyticsService;
    private final CourseEnrichmentService enrichmentService;

    public AdminDashboardController(AnalyticsService analyticsService,
                                   CourseEnrichmentService enrichmentService) {
        this.analyticsService = analyticsService;
        this.enrichmentService = enrichmentService;
    }

    /**
     * GET /admin/dashboard — The Academic Gap Index analytics dashboard.
     */
    @GetMapping
    public String analyticsDashboard(Model model) {
        // Stat cards
        model.addAttribute("totalStudents", analyticsService.getTotalStudentsCount());
        model.addAttribute("verifiedExperts", analyticsService.getVerifiedExpertsCount());
        model.addAttribute("approvedEnrichments", analyticsService.getApprovedEnrichmentsCount());
        model.addAttribute("pendingEnrichments", analyticsService.getPendingEnrichmentsCount());

        // Chart data — Top 5 Skills and Top 5 Tools
        List<ChartDataDTO> topSkills = analyticsService.getTopSkills(5);
        List<ChartDataDTO> topTools = analyticsService.getTopTools(5);
        model.addAttribute("topSkills", topSkills);
        model.addAttribute("topTools", topTools);

        return "admin/dashboard";
    }
}
