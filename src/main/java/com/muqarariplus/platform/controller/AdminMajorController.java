package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.College;
import com.muqarariplus.platform.entity.Major;
import com.muqarariplus.platform.payload.requests.MajorRequest;
import com.muqarariplus.platform.service.CollegeService;
import com.muqarariplus.platform.service.MajorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/academic/majors")
public class AdminMajorController {

    private final MajorService majorService;
    private final CollegeService collegeService;

    public AdminMajorController(MajorService majorService, CollegeService collegeService) {
        this.majorService = majorService;
        this.collegeService = collegeService;
    }

    @GetMapping
    public String listMajors(@RequestParam(required = false) Long editId, Model model) {
        List<Major> majors = majorService.getAllMajors();
        List<College> colleges = collegeService.getAllColleges();
        model.addAttribute("majors", majors);
        model.addAttribute("colleges", colleges);
        if (editId != null) {
            model.addAttribute("editingMajor", majorService.getMajorById(editId));
        } else {
            model.addAttribute("editingMajor", null);
        }
        return "admin/academic-majors";
    }

    @PostMapping("/create")
    public String createMajor(@RequestParam Long collegeId,
                              @RequestParam String nameEn,
                              @RequestParam String nameAr,
                              @RequestParam(required = false) String code,
                              RedirectAttributes redirectAttributes) {
        try {
            majorService.createMajor(new MajorRequest(collegeId, nameEn, nameAr, code));
            redirectAttributes.addFlashAttribute("successMsg", "Major created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to create major: " + e.getMessage());
        }
        return "redirect:/admin/academic/majors";
    }

    @PostMapping("/{id}/update")
    public String updateMajor(@PathVariable Long id,
                              @RequestParam Long collegeId,
                              @RequestParam String nameEn,
                              @RequestParam String nameAr,
                              @RequestParam(required = false) String code,
                              RedirectAttributes redirectAttributes) {
        try {
            majorService.updateMajor(id, new MajorRequest(collegeId, nameEn, nameAr, code));
            redirectAttributes.addFlashAttribute("successMsg", "Major updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update major: " + e.getMessage());
        }
        return "redirect:/admin/academic/majors";
    }

    @PostMapping("/{id}/delete")
    public String deleteMajor(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            majorService.deleteMajor(id);
            redirectAttributes.addFlashAttribute("successMsg", "Major deleted successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete major: " + e.getMessage());
        }
        return "redirect:/admin/academic/majors";
    }
}
