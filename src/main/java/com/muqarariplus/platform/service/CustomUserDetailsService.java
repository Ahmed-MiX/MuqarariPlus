package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user;

        if (identifier.contains("@")) {
            // It's an email - only Students & Experts allowed.
            user = userRepository.findByEmail(identifier);
            if (user != null && (user.getRole().equals("ROLE_ADMIN") || user.getRole().equals("ROLE_SUPER_ADMIN"))) {
                throw new UsernameNotFoundException("Admins cannot login via email.");
            }
        } else {
            // It's a username - only Admins & Super Admins allowed.
            user = userRepository.findByUsername(identifier);
            if (user != null && (!user.getRole().equals("ROLE_ADMIN") && !user.getRole().equals("ROLE_SUPER_ADMIN"))) {
                throw new UsernameNotFoundException("Students and Experts must use email to login.");
            }
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + identifier);
        }

        // Optional: Block pending experts from logging in
        if ("PENDING".equals(user.getStatus())) {
             throw new UsernameNotFoundException("Expert account is pending approval.");
        }

        return new org.springframework.security.core.userdetails.User(
                identifier,
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
