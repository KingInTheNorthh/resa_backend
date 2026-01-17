package com.example.ecommerce.web;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.AppUserService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.web.dto.ProductRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final AppUserService appUserService;

    public ProductController(ProductService productService, AppUserService appUserService) {
        this.productService = productService;
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<Product> listProducts() {
        return productService.listProducts();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OWNER') or hasRole('SELLER')")
    public Product createProduct(@Valid @RequestBody ProductRequest request, Authentication authentication) {
        AppUser seller = appUserService.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return productService.createProduct(request, seller);
    }
}
