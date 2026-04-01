package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.Skill;
import com.muqarariplus.platform.entity.Tool;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.SkillRepository;
import com.muqarariplus.platform.repository.ToolRepository;
import com.muqarariplus.platform.service.CourseEnrichmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/expert/enrichment")
public class ExpertEnrichmentController {

    private final CourseEnrichmentService enrichmentService;
    private final CourseRepository courseRepository;
    private final SkillRepository skillRepository;
    private final ToolRepository toolRepository;

    public ExpertEnrichmentController(CourseEnrichmentService enrichmentService,
                                     CourseRepository courseRepository,
                                     SkillRepository skillRepository,
                                     ToolRepository toolRepository) {
        this.enrichmentService = enrichmentService;
        this.courseRepository = courseRepository;
        this.skillRepository = skillRepository;
        this.toolRepository = toolRepository;
    }

    /**
     * GET /expert/enrichment/new — Displays the enrichment submission form.
     * Loads all available Courses, Skills, and Tools for the selection UI.
     */
    @GetMapping("/new")
    public String showEnrichmentForm(Model model) {
        List<Course> courses = courseRepository.findAll();
        List<Skill> skills = skillRepository.findAll();
        List<Tool> tools = toolRepository.findAll();

        model.addAttribute("courses", courses);
        model.addAttribute("skills", skills);
        model.addAttribute("tools", tools);

        return "expert/add-enrichment";
    }

    /**
     * POST /expert/enrichment/new — Processes the enrichment submission.
     * Retrieves the expert's identity from the Security Principal.
     */
    @PostMapping("/new")
    public String submitEnrichment(@RequestParam("courseId") Long courseId,
                                   @RequestParam("content") String content,
                                   @RequestParam(value = "skillIds", required = false) List<Long> skillIds,
                                   @RequestParam(value = "toolIds", required = false) List<Long> toolIds,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            enrichmentService.createEnrichment(
                    principal.getName(),
                    courseId,
                    content,
                    skillIds,
                    toolIds
            );
            redirectAttributes.addFlashAttribute("successMsg", "enrichment_submitted");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/expert";
    }
}
