package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.CourseAdditionRequest;
import com.muqarariplus.platform.entity.CourseAdditionRequestStatus;
import com.muqarariplus.platform.service.CourseAdditionRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/course-requests")
public class AdminCourseRequestController {

    private final CourseAdditionRequestService requestService;

    public AdminCourseRequestController(CourseAdditionRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public String viewRequests(@RequestParam(required = false) String status, Model model) {
        List<CourseAdditionRequest> requests;
        CourseAdditionRequestStatus selectedStatus = null;

        if (status != null && !status.isBlank()) {
            try {
                selectedStatus = CourseAdditionRequestStatus.valueOf(status.toUpperCase());
                requests = requestService.getRequestsByStatus(selectedStatus);
            } catch (IllegalArgumentException e) {
                requests = requestService.getAllRequests();
            }
        } else {
            requests = requestService.getAllRequests();
        }

        long pendingCount = requestService.getRequestsByStatus(CourseAdditionRequestStatus.PENDING).size();
        long approvedCount = requestService.getRequestsByStatus(CourseAdditionRequestStatus.APPROVED).size();
        long rejectedCount = requestService.getRequestsByStatus(CourseAdditionRequestStatus.REJECTED).size();

        model.addAttribute("requests", requests);
        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("totalCount", requests.size());

        return "admin/course-requests";
    }

    @PostMapping("/{id}/approve")
    public String approveRequest(@PathVariable Long id,
                                 @RequestParam(required = false) String adminNotes,
                                 RedirectAttributes redirectAttributes) {
        try {
            requestService.approveRequest(id, adminNotes);
            redirectAttributes.addFlashAttribute("successMsg", "Request approved successfully. Course created.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to approve: " + e.getMessage());
        }
        return "redirect:/admin/course-requests";
    }

    @PostMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(required = false) String adminNotes,
                                RedirectAttributes redirectAttributes) {
        try {
            requestService.rejectRequest(id, adminNotes);
            redirectAttributes.addFlashAttribute("successMsg", "Request rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to reject: " + e.getMessage());
        }
        return "redirect:/admin/course-requests";
    }

    @PostMapping("/{id}/delete")
    public String deleteRequest(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            requestService.deleteRequest(id);
            redirectAttributes.addFlashAttribute("successMsg", "Request deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete: " + e.getMessage());
        }
        return "redirect:/admin/course-requests";
    }
}
