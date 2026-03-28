package com.muqarariplus.platform.repository;

import com.muqarariplus.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByUsername(String username);
    long countByRole(String role);
    long countByRoleAndStatus(String role, String status);
    List<User> findByRole(String role);
    List<User> findByRoleAndStatus(String role, String status);
}
