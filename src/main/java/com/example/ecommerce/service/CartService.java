package com.example.ecommerce.service;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.CartStatus;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public Cart getOrCreateActiveCart(AppUser user) {
        return cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE)
            .orElseGet(() -> {
                Cart cart = new Cart();
                cart.setUser(user);
                cart.setStatus(CartStatus.ACTIVE);
                return cartRepository.save(cart);
            });
    }

    @Transactional
    public CartItem addItem(AppUser user, Product product, int quantity, BigDecimal unitPrice) {
        Cart cart = getOrCreateActiveCart(user);
        Optional<CartItem> existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        }
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return cartItemRepository.save(item);
    }
}
