package com.example.ecommerce.web;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.service.AppUserService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('OWNER')")
public class AdminController {

    private final AppUserService appUserService;

    public AdminController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/sellers")
    public List<AppUser> listSellers() {
        return appUserService.listSellers();
    }

    @PostMapping("/sellers/{id}/approve")
    public AppUser approveSeller(@PathVariable Long id) {
        return appUserService.verifySeller(id);
    }
}
