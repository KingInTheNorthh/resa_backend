package com.example.ecommerce.web;

import com.example.ecommerce.service.AppUserService;
import com.example.ecommerce.service.JwtService;
import com.example.ecommerce.web.dto.AuthResponse;
import com.example.ecommerce.web.dto.LoginRequest;
import com.example.ecommerce.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AppUserService appUserService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.appUserService = appUserService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        appUserService.register(request.getEmail(), request.getPassword());
        UserDetails userDetails = appUserService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token);
    }

    @PostMapping("/register-seller")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerSeller(@Valid @RequestBody RegisterRequest request) {
        appUserService.registerSeller(request.getEmail(), request.getPassword());
        UserDetails userDetails = appUserService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token);
    }
}
