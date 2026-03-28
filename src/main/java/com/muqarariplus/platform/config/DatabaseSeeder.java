package com.muqarariplus.platform.config;

import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@muqarariplus.com") == null) {
            User admin = new User();
            admin.setEmail("admin@muqarariplus.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Super");
            admin.setLastName("Admin");
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            System.out.println("SEEDER: Admin User created.");
        }
        
        if (userRepository.findByEmail("expert@muqarariplus.com") == null) {
            User expert = new User();
            expert.setEmail("expert@muqarariplus.com");
            expert.setPassword(passwordEncoder.encode("expert123"));
            expert.setFirstName("Tariq");
            expert.setLastName("Expert");
            expert.setRole("ROLE_EXPERT");
            userRepository.save(expert);
            System.out.println("SEEDER: Expert User created.");
        }
    }
}
