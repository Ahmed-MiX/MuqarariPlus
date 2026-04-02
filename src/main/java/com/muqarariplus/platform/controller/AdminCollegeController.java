package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.College;
import com.muqarariplus.platform.entity.University;
import com.muqarariplus.platform.payload.requests.CollegeRequest;
import com.muqarariplus.platform.service.CollegeService;
import com.muqarariplus.platform.service.UniversityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/academic/colleges")
public class AdminCollegeController {

    private final CollegeService collegeService;
    private final UniversityService universityService;

    public AdminCollegeController(CollegeService collegeService, UniversityService universityService) {
        this.collegeService = collegeService;
        this.universityService = universityService;
    }

    @GetMapping
    public String listColleges(@RequestParam(required = false) Long editId, Model model) {
        List<College> colleges = collegeService.getAllColleges();
        List<University> universities = universityService.getAllUniversities();
        model.addAttribute("colleges", colleges);
        model.addAttribute("universities", universities);
        if (editId != null) {
            model.addAttribute("editingCollege", collegeService.getCollegeById(editId));
        } else {
            model.addAttribute("editingCollege", null);
        }
        return "admin/academic-colleges";
    }

    @PostMapping("/create")
    public String createCollege(@RequestParam Long universityId,
                                @RequestParam String nameEn,
                                @RequestParam String nameAr,
                                RedirectAttributes redirectAttributes) {
        try {
            collegeService.createCollege(new CollegeRequest(universityId, nameEn, nameAr));
            redirectAttributes.addFlashAttribute("successMsg", "College created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to create college: " + e.getMessage());
        }
        return "redirect:/admin/academic/colleges";
    }

    @PostMapping("/{id}/update")
    public String updateCollege(@PathVariable Long id,
                                @RequestParam Long universityId,
                                @RequestParam String nameEn,
                                @RequestParam String nameAr,
                                RedirectAttributes redirectAttributes) {
        try {
            collegeService.updateCollege(id, new CollegeRequest(universityId, nameEn, nameAr));
            redirectAttributes.addFlashAttribute("successMsg", "College updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update college: " + e.getMessage());
        }
        return "redirect:/admin/academic/colleges";
    }

    @PostMapping("/{id}/delete")
    public String deleteCollege(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            collegeService.deleteCollege(id);
            redirectAttributes.addFlashAttribute("successMsg", "College deleted successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete college: " + e.getMessage());
        }
        return "redirect:/admin/academic/colleges";
    }
}
