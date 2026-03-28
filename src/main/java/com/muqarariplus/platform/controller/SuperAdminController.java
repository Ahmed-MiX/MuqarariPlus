package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.CourseEnrichmentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class SuperAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/super-admin")
    public String superAdminDashboard(Model model) {
        model.addAttribute("admins", userRepository.findByRole("ROLE_ADMIN"));
        return "super-admin";
    }

    @PostMapping("/super-admin/create-admin")
    public String createAdmin(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String email, Model model) {
        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Email already in use.");
            model.addAttribute("admins", userRepository.findByRole("ROLE_ADMIN"));
            return "super-admin";
        }

        User admin = new User();
        String tempUsername = "admin" + String.format("%04d", new java.util.Random().nextInt(10000));
        String tempPass = UUID.randomUUID().toString().substring(0, 8);

        admin.setUsername(tempUsername);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(tempPass));
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setRole("ROLE_ADMIN");
        admin.setStatus("APPROVED");

        userRepository.save(admin);

        model.addAttribute("newUsername", tempUsername);
        model.addAttribute("newPassword", tempPass);
        model.addAttribute("admins", userRepository.findByRole("ROLE_ADMIN"));

        return "super-admin";
    }

    @PostMapping("/super-admin/delete-admin")
    public String deleteAdmin(@RequestParam Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin != null && "ROLE_ADMIN".equals(admin.getRole())) {
            userRepository.delete(admin);
        }
        return "redirect:/super-admin";
    }
}
