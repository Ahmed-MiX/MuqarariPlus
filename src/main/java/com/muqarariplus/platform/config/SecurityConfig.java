package com.muqarariplus.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // تفعيل حماية Method Security (كودك)
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(UserDetailsService userDetailsService, CustomAuthenticationSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Simplified for the prototype
                .authorizeHttpRequests(authz -> authz
                        // دمج مسار /student/api/** الخاص بالزملاء مع مساراتك الأساسية
                        .requestMatchers("/", "/courses", "/course/**", "/search", "/css/**", "/js/**", "/img/**", "/fonts/**", "/uploads/**", "/login", "/register", "/error", "/favicon.ico", "/student/api/**").permitAll()
                        // مسارات محرك الهويات المزدوجة (كودك)
                        .requestMatchers("/u/**", "/expert-profile/**").permitAll()
                        // مسارات سجل المراقبة (كودك)
                        .requestMatchers("/api/admin/audit", "/api/admin/audit/**").hasRole("SUPER_ADMIN")
                        // مسارات الإدارة العليا والمشتركة
                        .requestMatchers("/super-admin", "/super-admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/expert", "/expert/**").hasAnyRole("EXPERT", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/admin", "/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/student-dashboard", "/student-dashboard/**", "/student", "/student/**", "/api/engagement/**").hasAnyRole("STUDENT", "EXPERT", "ADMIN", "SUPER_ADMIN")
                        // مسار الخزنة الخاصة (كودك)
                        .requestMatchers("/profile/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}