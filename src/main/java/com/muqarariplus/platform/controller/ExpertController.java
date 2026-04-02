package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.dto.EnrichmentSubmissionDTO;
import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.CourseEnrichment;
import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.entity.ExpertStatus;
import com.muqarariplus.platform.entity.ProfessionalCertificate;
import com.muqarariplus.platform.entity.Tool;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.ProfessionalCertificateRepository;
import com.muqarariplus.platform.repository.ToolRepository;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.service.CourseEnrichmentService;
import com.muqarariplus.platform.service.EnrichmentService;
import com.muqarariplus.platform.service.ExpertService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * THE EXPERT VANGUARD — Expert Portal Controller
 * Handles expert verification, dashboard, and the enrichment
 * submission portal with full Bean Validation support.
 * ═══════════════════════════════════════════════════════════════════
 */
@Controller
@RequestMapping("/expert")
@PreAuthorize("hasRole('EXPERT')")
public class ExpertController {

    private final ExpertService expertService;
    private final UserRepository userRepository;
    private final CourseEnrichmentService enrichmentService;
    private final EnrichmentService submissionService;
    private final CourseRepository courseRepository;
    private final ToolRepository toolRepository;
    private final ProfessionalCertificateRepository certificateRepository;

    public ExpertController(ExpertService expertService,
                            UserRepository userRepository,
                            CourseEnrichmentService enrichmentService,
                            EnrichmentService submissionService,
                            CourseRepository courseRepository,
                            ToolRepository toolRepository,
                            ProfessionalCertificateRepository certificateRepository) {
        this.expertService = expertService;
        this.userRepository = userRepository;
        this.enrichmentService = enrichmentService;
        this.submissionService = submissionService;
        this.courseRepository = courseRepository;
        this.toolRepository = toolRepository;
        this.certificateRepository = certificateRepository;
    }

    // ═══════════════════════════════════════════════════════════════
    // EXPERT DASHBOARD — Verification status, impact metrics
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("")
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

    // ═══════════════════════════════════════════════════════════════
    // POST /expert/verify — Submit verification documents
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/verify")
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

    // ═══════════════════════════════════════════════════════════════
    // GET /expert/enrich/new — Show Enrichment Submission Form
    // Only APPROVED experts may access this form.
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/enrich/new")
    public String showEnrichmentForm(Model model, Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        User user = resolveUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        Expert expert = expertService.getOrCreateExpert(user);

        // Gate: Only APPROVED experts may submit enrichments
        if (expert.getStatus() != ExpertStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "⛔ يجب أن يكون حسابك معتمداً من الإدارة لإرسال إثراء — Your expert account must be APPROVED before submitting enrichments.");
            return "redirect:/expert";
        }

        // Inject all required model data
        List<Course> courses = courseRepository.findAll();
        List<Tool> tools = toolRepository.findAll();
        List<ProfessionalCertificate> certificates = certificateRepository.findAll();

        model.addAttribute("courses", courses);
        model.addAttribute("tools", tools);
        model.addAttribute("certificates", certificates);

        // Inject empty DTO if not already present (for fresh form load)
        if (!model.containsAttribute("enrichmentDTO")) {
            model.addAttribute("enrichmentDTO", new EnrichmentSubmissionDTO());
        }

        return "expert/enrichment-form";
    }

    // ═══════════════════════════════════════════════════════════════
    // POST /expert/enrich — Process Enrichment Submission
    // Uses @Valid + BindingResult for server-side Bean Validation.
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/enrich")
    public String processEnrichment(@Valid @ModelAttribute("enrichmentDTO") EnrichmentSubmissionDTO dto,
                                    BindingResult bindingResult,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {

        // ── Handle Validation Errors ──
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.enrichmentDTO", bindingResult);
            redirectAttributes.addFlashAttribute("enrichmentDTO", dto);
            redirectAttributes.addFlashAttribute("errorMsg",
                    "⚠️ يرجى تصحيح الأخطاء أدناه — Please correct the validation errors below.");
            return "redirect:/expert/enrich/new";
        }

        // ── Submit via Service ──
        try {
            String expertIdentifier = authentication.getName();
            submissionService.submitNewEnrichment(expertIdentifier, dto);
            redirectAttributes.addFlashAttribute("successMsg",
                    "✅ تم إرسال الإثراء بنجاح وهو بانتظار المراجعة! — Enrichment submitted successfully and is pending review!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "❌ حدث خطأ غير متوقع — An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/expert/enrich/new";
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER: Resolve User from Spring Security Authentication
    // Supports both email-based and username-based login identifiers.
    // ═══════════════════════════════════════════════════════════════
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
