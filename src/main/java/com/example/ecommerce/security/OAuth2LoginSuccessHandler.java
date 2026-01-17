package com.example.ecommerce.security;

import com.example.ecommerce.service.AppUserService;
import com.example.ecommerce.service.JwtService;
import com.example.ecommerce.web.dto.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserService appUserService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(AppUserService appUserService, JwtService jwtService, ObjectMapper objectMapper) {
        this.appUserService = appUserService;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth2 authentication failed");
            return;
        }

        OAuth2User oauthUser = oauthToken.getPrincipal();
        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not available from OAuth provider");
            return;
        }

        appUserService.getOrCreateOauthUser(email);
        UserDetails userDetails = appUserService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new AuthResponse(token)));
    }
}
