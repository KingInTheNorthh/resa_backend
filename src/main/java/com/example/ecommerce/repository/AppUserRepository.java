package com.example.ecommerce.repository;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByRole(Role role);
}
