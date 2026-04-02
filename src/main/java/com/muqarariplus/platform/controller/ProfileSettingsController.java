package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.audit.Auditable;
import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.ExpertRepository;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

/**
 * ═══════════════════════════════════════════════════════════════════
 * THE PRIVATE VAULT — Profile Settings Controller
 * Users can ONLY edit their own authenticated entity.
 * ═══════════════════════════════════════════════════════════════════
 */
@Controller
@RequestMapping("/profile")
public class ProfileSettingsController {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileSettingsController(UserRepository userRepository,
                                     ExpertRepository expertRepository,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.expertRepository = expertRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Helper: Resolve the currently authenticated User entity ──
    private User resolveCurrentUser(Principal principal) {
        String identifier = principal.getName();
        User user = userRepository.findByEmail(identifier);
        if (user == null) {
            user = userRepository.findByUsername(identifier);
        }
        if (user == null) {
            throw new IllegalArgumentException("User session invalid.");
        }
        return user;
    }

    // ═══════════════════════════════════════════════════════════════
    // GET /profile/settings — Display Settings Page
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/settings")
    public String settingsPage(Principal principal, Model model) {
        User user = resolveCurrentUser(principal);
        model.addAttribute("user", user);

        // If expert, also load expert entity
        if ("ROLE_EXPERT".equals(user.getRole())) {
            Optional<Expert> expertOpt = expertRepository.findByUserId(user.getId());
            expertOpt.ifPresent(expert -> model.addAttribute("expert", expert));
        }

        return "profile/settings";
    }

    // ═══════════════════════════════════════════════════════════════
    // POST /profile/settings/update — Update Basic Info & Social
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/settings/update")
    @Auditable(action = "UPDATE_PROFILE", entity = "User")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam(required = false) String bio,
                                @RequestParam(required = false) String linkedInUrl,
                                @RequestParam(required = false) String githubUrl,
                                @RequestParam(required = false) String bioAr,
                                @RequestParam(required = false) String bioEn,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        User user = resolveCurrentUser(principal);

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBio(bio);
        user.setLinkedInUrl(linkedInUrl);
        user.setGithubUrl(githubUrl);
        userRepository.save(user);

        // If expert, update expert-specific fields
        if ("ROLE_EXPERT".equals(user.getRole())) {
            Optional<Expert> expertOpt = expertRepository.findByUserId(user.getId());
            if (expertOpt.isPresent()) {
                Expert expert = expertOpt.get();
                expert.setBioAr(bioAr);
                expert.setBioEn(bioEn);
                expert.setLinkedinUrl(linkedInUrl);
                expert.setGithubUrl(githubUrl);
                expertRepository.save(expert);
            }
        }

        redirectAttributes.addFlashAttribute("successMsg", "Profile updated successfully! ✅");
        return "redirect:/profile/settings";
    }

    // ═══════════════════════════════════════════════════════════════
    // POST /profile/settings/security — Change Password
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/settings/security")
    @Auditable(action = "CHANGE_PASSWORD", entity = "User")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User user = resolveCurrentUser(principal);

        // Validate old password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Current password is incorrect. ❌");
            return "redirect:/profile/settings";
        }

        // Validate new password match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "New passwords do not match. ❌");
            return "redirect:/profile/settings";
        }

        // Validate minimum length
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMsg", "Password must be at least 6 characters. ❌");
            return "redirect:/profile/settings";
        }

        // Hash and save
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMsg", "Password changed successfully! 🔐");
        return "redirect:/profile/settings";
    }
}
