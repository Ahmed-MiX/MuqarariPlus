package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.University;
import com.muqarariplus.platform.payload.requests.UniversityRequest;
import com.muqarariplus.platform.service.UniversityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/academic/universities")
public class AdminUniversityController {

    private final UniversityService universityService;

    public AdminUniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @GetMapping
    public String listUniversities(@RequestParam(required = false) Long editId, Model model) {
        List<University> universities = universityService.getAllUniversities();
        model.addAttribute("universities", universities);
        if (editId != null) {
            model.addAttribute("editingUniversity", universityService.getUniversityById(editId));
        } else {
            model.addAttribute("editingUniversity", null);
        }
        return "admin/academic-universities";
    }

    @PostMapping("/create")
    public String createUniversity(@RequestParam String nameEn,
                                   @RequestParam String nameAr,
                                   RedirectAttributes redirectAttributes) {
        try {
            universityService.createUniversity(new UniversityRequest(nameEn, nameAr));
            redirectAttributes.addFlashAttribute("successMsg", "University created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to create university: " + e.getMessage());
        }
        return "redirect:/admin/academic/universities";
    }

    @PostMapping("/{id}/update")
    public String updateUniversity(@PathVariable Long id,
                                   @RequestParam String nameEn,
                                   @RequestParam String nameAr,
                                   RedirectAttributes redirectAttributes) {
        try {
            universityService.updateUniversity(id, new UniversityRequest(nameEn, nameAr));
            redirectAttributes.addFlashAttribute("successMsg", "University updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update university: " + e.getMessage());
        }
        return "redirect:/admin/academic/universities";
    }

    @PostMapping("/{id}/delete")
    public String deleteUniversity(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            universityService.deleteUniversity(id);
            redirectAttributes.addFlashAttribute("successMsg", "University deleted successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete university: " + e.getMessage());
        }
        return "redirect:/admin/academic/universities";
    }
}
