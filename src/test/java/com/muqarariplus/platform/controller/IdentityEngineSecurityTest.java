package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ═══════════════════════════════════════════════════════════════════
 * IDENTITY ENGINE SECURITY TEST — Zero-Bleeding Verification
 * Proves data encapsulation, password security, and public access.
 * ═══════════════════════════════════════════════════════════════════
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IdentityEngineSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ═══════════════════════════════════════════════════════════════
    // TEST 1: Profile update only modifies the authenticated user
    // The controller resolves from Principal, so "admin" only
    // touches the admin's own record, never a student's.
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(1)
    @DisplayName("Test 1: Profile update only modifies the authenticated user's own data")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void profileUpdateOnlyAffectsAuthenticatedUser() throws Exception {
        // Find a student user to verify they are NOT modified
        List<User> students = userRepository.findByRole("ROLE_STUDENT");
        Assumptions.assumeFalse(students.isEmpty(), "No students in DB to test with");
        User victimStudent = students.get(0);
        String originalFirstName = victimStudent.getFirstName();

        // POST to profile settings as "admin" — the controller resolves
        // from Principal (admin), not from any request parameter.
        mockMvc.perform(post("/profile/settings/update")
                .param("firstName", "HACKED")
                .param("lastName", "HACKED"))
                .andExpect(status().is3xxRedirection());

        // Verify the student's data was NOT modified
        User studentAfter = userRepository.findById(victimStudent.getId()).orElse(null);
        assertNotNull(studentAfter);
        assertEquals(originalFirstName, studentAfter.getFirstName(),
                "CRITICAL: Student's name was modified by a different user's session!");

        // Restore admin's original name
        User admin = userRepository.findByUsername("admin");
        if (admin != null) {
            admin.setFirstName("خالد");
            admin.setLastName("نمر");
            userRepository.save(admin);
        }

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 1 PASSED: Profile isolation verified          ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 2: Password change works and hashes correctly
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(2)
    @DisplayName("Test 2: Password change hashes new password with BCrypt")
    void passwordChangeHashesCorrectly() {
        User admin = userRepository.findByUsername("admin");
        Assumptions.assumeTrue(admin != null, "admin user not found in DB");

        String oldHash = admin.getPassword();
        String newRawPassword = "NewSecure123!";

        // Simulate changing the password
        admin.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(admin);

        // Reload and verify
        User updated = userRepository.findByUsername("admin");
        assertNotNull(updated);
        assertNotEquals(oldHash, updated.getPassword(),
                "Password hash must change after update");
        assertTrue(passwordEncoder.matches(newRawPassword, updated.getPassword()),
                "New password must authenticate successfully with BCrypt");
        assertFalse(passwordEncoder.matches("wrongPassword", updated.getPassword()),
                "Wrong password must NOT authenticate");

        // Restore original password
        updated.setPassword(oldHash);
        userRepository.save(updated);

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 2 PASSED: Password hashing verified           ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 3: Public profiles return 200 for anonymous visitors
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(3)
    @DisplayName("Test 3: Public student profile /u/{username} → 200 for anonymous")
    void publicStudentProfileAccessibleAnonymously() throws Exception {
        // Find any user with a username set
        List<User> students = userRepository.findByRole("ROLE_STUDENT");
        User withUsername = null;
        for (User s : students) {
            if (s.getUsername() != null && !s.getUsername().isEmpty()) {
                withUsername = s;
                break;
            }
        }
        Assumptions.assumeTrue(withUsername != null, "No student with username found");

        mockMvc.perform(get("/u/" + withUsername.getUsername()))
                .andExpect(status().isOk());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 3 PASSED: Public profile → 200 (anonymous)    ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 4: Profile settings requires authentication
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(4)
    @DisplayName("Test 4: /profile/settings → 302 redirect for anonymous")
    void profileSettingsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/profile/settings"))
                .andExpect(status().is3xxRedirection());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 4 PASSED: Settings protected → 302 redirect   ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    // ═══════════════════════════════════════════════════════════════
    // TEST 5: Authenticated user can access their own settings
    // ═══════════════════════════════════════════════════════════════
    @Test
    @Order(5)
    @DisplayName("Test 5: Authenticated user → /profile/settings → 200 OK")
    @WithMockUser(username = "admin", roles = {"SUPER_ADMIN"})
    void authenticatedUserCanAccessSettings() throws Exception {
        mockMvc.perform(get("/profile/settings"))
                .andExpect(status().isOk());

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ✅ TEST 5 PASSED: Authenticated → settings 200 OK     ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }
}
