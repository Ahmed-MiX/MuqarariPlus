package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.Expert;
import com.muqarariplus.platform.entity.ExpertStatus;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.repository.ExpertRepository;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;

    public CustomUserDetailsService(UserRepository userRepository, ExpertRepository expertRepository) {
        this.userRepository = userRepository;
        this.expertRepository = expertRepository;
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

        // Build authorities list
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

        // For experts with APPROVED status, grant additional ROLE_VERIFIED_EXPERT
        if ("ROLE_EXPERT".equals(user.getRole())) {
            Optional<Expert> expertOpt = expertRepository.findByUserId(user.getId());
            if (expertOpt.isPresent() && expertOpt.get().getStatus() == ExpertStatus.APPROVED) {
                authorities.add(new SimpleGrantedAuthority("ROLE_VERIFIED_EXPERT"));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                identifier,
                user.getPassword(),
                authorities
        );
    }
}
