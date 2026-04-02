package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.audit.Auditable;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.service.SuperAdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminDashboardService dashboardService;

    public SuperAdminController(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                SuperAdminDashboardService dashboardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dashboardService = dashboardService;
    }

    // ── Legacy root redirect ──
    @GetMapping("")
    public String root() { return "redirect:/super-admin/dashboard"; }

    // ═══════════════════════════════════════════════════════════════
    // UNIFIED COMMAND CENTER DASHBOARD
    // Serves: KPIs, Charts, Audit Trail, Admin Fleet, Generate Form
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // KPI data
        model.addAttribute("totalUsers", dashboardService.getTotalUsers());
        model.addAttribute("usersByRole", dashboardService.getUsersByRole());
        model.addAttribute("totalCourses", dashboardService.getTotalCourses());
        model.addAttribute("totalEnrichments", dashboardService.getTotalEnrichments());
        model.addAttribute("enrichmentsByStatus", dashboardService.getEnrichmentsByStatus());

        // Audit trail
        model.addAttribute("auditLogs", dashboardService.getLatestAuditLogs());

        // Admin fleet (for Generate Admins section)
        List<User> admins = userRepository.findByRole("ROLE_ADMIN");
        model.addAttribute("admins", admins);

        return "super-admin/dashboard";
    }

    // ═══════════════════════════════════════════════════════════════
    // USER MATRIX
    // ═══════════════════════════════════════════════════════════════
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", dashboardService.getAllUsers());
        return "super-admin/users";
    }

    // ── Toggle User Status (ACTIVE/SUSPENDED) via AJAX ──
    @PostMapping("/users/{id}/toggle-status")
    @ResponseBody
    @Auditable(action = "TOGGLE_STATUS", entity = "User")
    public ResponseEntity<Map<String, String>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if ("ROLE_SUPER_ADMIN".equals(user.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot modify Super Admin"));
        }

        String newStatus = "SUSPENDED".equals(user.getStatus()) ? "APPROVED" : "SUSPENDED";
        user.setStatus(newStatus);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("status", newStatus, "userId", String.valueOf(id)));
    }

    // ── Change User Role via AJAX ──
    @PostMapping("/users/{id}/change-role")
    @ResponseBody
    @Auditable(action = "CHANGE_ROLE", entity = "User")
    public ResponseEntity<Map<String, String>> changeUserRole(@PathVariable Long id,
                                                               @RequestParam String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if ("ROLE_SUPER_ADMIN".equals(user.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot modify Super Admin"));
        }
        if (!newRole.startsWith("ROLE_")) {
            newRole = "ROLE_" + newRole;
        }

        user.setRole(newRole);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("role", newRole, "userId", String.valueOf(id)));
    }

    // ═══════════════════════════════════════════════════════════════
    // GENERATE ADMIN — Creates admin and returns credentials via
    // flash attributes so they display on the dashboard redirect.
    // ═══════════════════════════════════════════════════════════════
    @PostMapping("/create-admin")
    @Auditable(action = "CREATE", entity = "User")
    public String createAdmin(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String email,
                              RedirectAttributes redirectAttributes) {
        if (userRepository.findByEmail(email) != null) {
            redirectAttributes.addFlashAttribute("error", "Email already in use.");
            return "redirect:/super-admin/dashboard";
        }

        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        String username = "admin" + String.format("%04d", new java.util.Random().nextInt(10000));

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setRole("ROLE_ADMIN");
        admin.setStatus("APPROVED");
        userRepository.save(admin);

        // Pass generated credentials via flash attributes
        redirectAttributes.addFlashAttribute("newUsername", username);
        redirectAttributes.addFlashAttribute("newPassword", rawPassword);

        return "redirect:/super-admin/dashboard";
    }

    // ── Delete Admin ──
    @PostMapping("/delete-admin")
    @Auditable(action = "DELETE", entity = "User")
    public String deleteAdmin(@RequestParam Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin != null && "ROLE_ADMIN".equals(admin.getRole())) {
            userRepository.delete(admin);
        }
        return "redirect:/super-admin/dashboard";
    }
}
