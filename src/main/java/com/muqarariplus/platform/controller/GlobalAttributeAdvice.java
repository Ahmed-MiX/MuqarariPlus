package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.service.ContentService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

/**
 * GlobalAttributeAdvice injects the DB-driven content map into EVERY
 * Thymeleaf model automatically, so templates can use ${content['key']}
 * without any per-controller wiring.
 */
@ControllerAdvice
public class GlobalAttributeAdvice {

    private final ContentService contentService;

    public GlobalAttributeAdvice(ContentService contentService) {
        this.contentService = contentService;
    }

    @ModelAttribute
    public void injectContent(Model model, Locale locale) {
        model.addAttribute("content", contentService.getContentMap(locale));
    }
}
