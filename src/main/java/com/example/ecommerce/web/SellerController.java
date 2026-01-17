package com.example.ecommerce.web;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.AppUserService;
import com.example.ecommerce.service.ProductService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerController {

    private final AppUserService appUserService;
    private final ProductService productService;

    public SellerController(AppUserService appUserService, ProductService productService) {
        this.appUserService = appUserService;
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<Product> listMyProducts(Authentication authentication) {
        AppUser seller = appUserService.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return productService.listSellerProducts(seller.getId());
    }
}
