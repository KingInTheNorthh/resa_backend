package com.example.ecommerce.service;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.AuthProvider;
import com.example.ecommerce.model.Role;
import com.example.ecommerce.repository.AppUserRepository;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(String email, String rawPassword) {
        appUserRepository.findByEmail(email).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        });

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.CUSTOMER);
        user.setProvider(AuthProvider.LOCAL);
        return appUserRepository.save(user);
    }

    public AppUser registerSeller(String email, String rawPassword) {
        AppUser user = register(email, rawPassword);
        user.setRole(Role.SELLER);
        user.setSellerVerified(false);
        return appUserRepository.save(user);
    }

    public AppUser getOrCreateOauthUser(String email) {
        return appUserRepository.findByEmail(email).orElseGet(() -> {
            AppUser user = new AppUser();
            user.setEmail(email);
            user.setPassword(null);
            user.setRole(Role.CUSTOMER);
            user.setProvider(AuthProvider.GOOGLE);
            return appUserRepository.save(user);
        });
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public AppUser verifySeller(Long id) {
        AppUser user = appUserRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != Role.SELLER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a seller");
        }
        user.setSellerVerified(true);
        return appUserRepository.save(user);
    }

    public java.util.List<AppUser> listSellers() {
        return appUserRepository.findByRole(Role.SELLER);
    }

    public AppUser createOwnerIfMissing(String email, String rawPassword) {
        return appUserRepository.findByEmail(email).orElseGet(() -> {
            AppUser user = new AppUser();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(Role.OWNER);
            user.setProvider(AuthProvider.LOCAL);
            user.setSellerVerified(true);
            return appUserRepository.save(user);
        });
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = appUserRepository.findByEmail(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String password = user.getPassword() == null ? "" : user.getPassword();
        return User.withUsername(user.getEmail())
            .password(password)
            .roles(user.getRole().name())
            .build();
    }
}
