package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.Major;
import com.muqarariplus.platform.payload.requests.CourseRequest;
import com.muqarariplus.platform.service.CourseService;
import com.muqarariplus.platform.service.MajorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/academic/courses")
public class AdminCourseController {

    private final CourseService courseService;
    private final MajorService majorService;

    public AdminCourseController(CourseService courseService, MajorService majorService) {
        this.courseService = courseService;
        this.majorService = majorService;
    }

    @GetMapping
    public String listCourses(@RequestParam(required = false) Long majorId,
                              @RequestParam(required = false) Long editId, Model model) {
        List<Major> majors = majorService.getAllMajors();
        model.addAttribute("majors", majors);

        if (majorId != null) {
            List<Course> courses = courseService.getCoursesByMajorId(majorId);
            model.addAttribute("courses", courses);
            model.addAttribute("selectedMajorId", majorId);
        } else if (!majors.isEmpty()) {
            Long firstMajorId = majors.get(0).getId();
            List<Course> courses = courseService.getCoursesByMajorId(firstMajorId);
            model.addAttribute("courses", courses);
            model.addAttribute("selectedMajorId", firstMajorId);
        }

        if (editId != null) {
            model.addAttribute("editingCourse", courseService.getCourseById(editId));
        } else {
            model.addAttribute("editingCourse", null);
        }

        model.addAttribute("editingCourse", null);
        return "admin/academic-courses";
    }

    @PostMapping("/create")
    public String createCourse(@RequestParam Long majorId,
                               @RequestParam String code,
                               @RequestParam String nameEn,
                               @RequestParam String nameAr,
                               @RequestParam(required = false) String descriptionEn,
                               @RequestParam(required = false) String descriptionAr,
                               @RequestParam(required = false) String syllabusUrl,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.createCourse(new CourseRequest(code, nameEn, nameAr, descriptionEn, descriptionAr, syllabusUrl, majorId));
            redirectAttributes.addFlashAttribute("successMsg", "Course created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to create course: " + e.getMessage());
        }
        return "redirect:/admin/academic/courses?majorId=" + majorId;
    }

    @PostMapping("/{id}/update")
    public String updateCourse(@PathVariable Long id,
                               @RequestParam Long majorId,
                               @RequestParam String code,
                               @RequestParam String nameEn,
                               @RequestParam String nameAr,
                               @RequestParam(required = false) String descriptionEn,
                               @RequestParam(required = false) String descriptionAr,
                               @RequestParam(required = false) String syllabusUrl,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.updateCourse(id, new CourseRequest(code, nameEn, nameAr, descriptionEn, descriptionAr, syllabusUrl, majorId));
            redirectAttributes.addFlashAttribute("successMsg", "Course updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update course: " + e.getMessage());
        }
        return "redirect:/admin/academic/courses?majorId=" + majorId;
    }

    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id,
                               @RequestParam Long majorId,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("successMsg", "Course deleted successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete course: " + e.getMessage());
        }
        return "redirect:/admin/academic/courses?majorId=" + majorId;
    }
}
