package com.example.ecommerce.web;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.AppUserService;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.web.dto.AddToCartRequest;
import com.example.ecommerce.web.dto.CartItemResponse;
import com.example.ecommerce.web.dto.CartResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final AppUserService appUserService;
    private final ProductService productService;
    private final CartService cartService;

    public CartController(AppUserService appUserService, ProductService productService, CartService cartService) {
        this.appUserService = appUserService;
        this.productService = productService;
        this.cartService = cartService;
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse addToCart(@Valid @RequestBody AddToCartRequest request, Authentication authentication) {
        AppUser user = appUserService.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Product product = productService.getProduct(request.getProductId());
        CartItem item = cartService.addItem(user, product, request.getQuantity(), productService.priceFor(product));
        return toItemResponse(item);
    }

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        AppUser user = appUserService.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Cart cart = cartService.getOrCreateActiveCart(user);
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        List<CartItemResponse> items = cart.getItems().stream()
            .map(this::toItemResponse)
            .collect(Collectors.toList());
        response.setItems(items);
        return response;
    }

    private CartItemResponse toItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        return response;
    }
}
