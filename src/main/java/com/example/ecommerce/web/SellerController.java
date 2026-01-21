package com.example.ecommerce.web;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductImage;
import com.example.ecommerce.service.AppUserService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.web.dto.ProductImageResponse;
import com.example.ecommerce.web.dto.ProductResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    public List<ProductResponse> listMyProducts(Authentication authentication) {
        AppUser seller = appUserService.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        List<Product> products = productService.listSellerProducts(seller.getId());
        if (products.isEmpty()) {
            return List.of();
        }
        Map<Long, List<ProductImage>> imagesByProductId = productService.listImagesByProductIds(
            products.stream().map(Product::getId).collect(Collectors.toList())
        );
        return products.stream()
            .map(product -> toResponse(product, imagesByProductId.get(product.getId())))
            .collect(Collectors.toList());
    }

    private ProductResponse toResponse(Product product, List<ProductImage> images) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        if (images == null || images.isEmpty()) {
            response.setImages(List.of());
            return response;
        }
        response.setImages(images.stream()
            .map(image -> new ProductImageResponse(image.getId(), "/api/products/images/" + image.getId()))
            .collect(Collectors.toList()));
        return response;
    }
}
