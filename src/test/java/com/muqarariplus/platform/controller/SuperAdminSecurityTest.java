package com.muqarariplus.platform.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ═══════════════════════════════════════════════════════════════════
 * IRON-CLAD RBAC ISOLATION TEST — Super Admin Security Verification
 * Proves absolute role-based access control on /super-admin/** routes.
 * ═══════════════════════════════════════════════════════════════════
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SuperAdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ═══════════════════════════════════════════════════════════════
    // TEST 1: ROLE_SUPER_ADMIN → 200 OK (Access Granted)
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(1)
    @DisplayName("Test 1: SUPER_ADMIN can access /super-admin/dashboard → HTTP 200")
    @WithMockUser(username = "superadmin@psau.edu.sa", roles = {"SUPER_ADMIN"})
    void superAdminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/super-admin/dashboard"))
                .andExpect(status().isOk());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 1 PASSED: SUPER_ADMIN → 200 OK               ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 2: ROLE_ADMIN → 403 FORBIDDEN (Access Denied)
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(2)
    @DisplayName("Test 2: ADMIN gets 403 Forbidden on /super-admin/dashboard")
    @WithMockUser(username = "admin@psau.edu.sa", roles = {"ADMIN"})
    void adminCannotAccessSuperAdminDashboard() throws Exception {
        mockMvc.perform(get("/super-admin/dashboard"))
                .andExpect(status().isForbidden());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 2 PASSED: ADMIN → 403 FORBIDDEN              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 3: ROLE_STUDENT → 403 FORBIDDEN (Access Denied)
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(3)
    @DisplayName("Test 3: STUDENT gets 403 Forbidden on /super-admin/dashboard")
    @WithMockUser(username = "student@psau.edu.sa", roles = {"STUDENT"})
    void studentCannotAccessSuperAdminDashboard() throws Exception {
        mockMvc.perform(get("/super-admin/dashboard"))
                .andExpect(status().isForbidden());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 3 PASSED: STUDENT → 403 FORBIDDEN            ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 4: ROLE_SUPER_ADMIN can access /super-admin/users → 200
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(4)
    @DisplayName("Test 4: SUPER_ADMIN can access /super-admin/users → HTTP 200")
    @WithMockUser(username = "superadmin@psau.edu.sa", roles = {"SUPER_ADMIN"})
    void superAdminCanAccessUsersPage() throws Exception {
        mockMvc.perform(get("/super-admin/users"))
                .andExpect(status().isOk());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 4 PASSED: SUPER_ADMIN → /users 200 OK        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 5: ROLE_EXPERT → 403 FORBIDDEN on /super-admin/users
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(5)
    @DisplayName("Test 5: EXPERT gets 403 Forbidden on /super-admin/users")
    @WithMockUser(username = "expert@psau.edu.sa", roles = {"EXPERT"})
    void expertCannotAccessSuperAdminUsers() throws Exception {
        mockMvc.perform(get("/super-admin/users"))
                .andExpect(status().isForbidden());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 5 PASSED: EXPERT → 403 FORBIDDEN             ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 6: Unauthenticated user → 302 Redirect to /login
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(6)
    @DisplayName("Test 6: Anonymous user redirected to login from /super-admin/dashboard")
    void anonymousUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/super-admin/dashboard"))
                .andExpect(status().is3xxRedirection());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 6 PASSED: ANONYMOUS → 302 REDIRECT           ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }
}
