package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.entity.ExpertStatus;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.service.ContentService;
import com.muqarariplus.platform.service.ExpertService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;
import java.util.Optional;

/**
 * GlobalAttributeAdvice injects the DB-driven content map AND expert
 * verification status into EVERY Thymeleaf model automatically.
 */
@ControllerAdvice
public class GlobalAttributeAdvice {

    private final ContentService contentService;
    private final UserRepository userRepository;
    private final ExpertService expertService;

    public GlobalAttributeAdvice(ContentService contentService,
                                 UserRepository userRepository,
                                 ExpertService expertService) {
        this.contentService = contentService;
        this.userRepository = userRepository;
        this.expertService = expertService;
    }

    @ModelAttribute
    public void injectContent(Model model, Locale locale) {
        model.addAttribute("content", contentService.getContentMap(locale));
    }

    /**
     * Injects expert verification status into the model for every request.
     * This allows any template to display notifications or conditionally
     * show/hide elements based on the expert's current verification state.
     */
    @ModelAttribute
    public void injectExpertStatus(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return;
        }

        String identifier = auth.getName();
        User user = userRepository.findByEmail(identifier);
        if (user == null) {
            user = userRepository.findByUsername(identifier);
        }
        if (user == null || !"ROLE_EXPERT".equals(user.getRole())) {
            return;
        }

        // Check expert entity status
        Optional<Expert> expertOpt = expertService.getExpertByEmail(user.getEmail());
        if (expertOpt.isPresent()) {
            Expert expert = expertOpt.get();
            model.addAttribute("globalExpertStatus", expert.getStatus().name());
            model.addAttribute("globalIsExpertApproved", expert.getStatus() == ExpertStatus.APPROVED);
            model.addAttribute("globalIsExpertRejected", expert.getStatus() == ExpertStatus.REJECTED);
            model.addAttribute("globalIsExpertPending", expert.getStatus() == ExpertStatus.PENDING);
        }
    }
}
