package com.muqarariplus.platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExpertController {

    @GetMapping("/expert")
    public String portalDashboard(Model model) {
        return "expert-portal";
    }


}
