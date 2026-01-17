package com.example.ecommerce.service;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.Role;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.web.dto.ProductRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Product createProduct(ProductRequest request, AppUser seller) {
        if (seller.getRole() == Role.SELLER && !seller.isSellerVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller is not verified");
        }
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSeller(seller);
        return productRepository.save(product);
    }

    public List<Product> listSellerProducts(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public void reduceStock(Product product, int quantity) {
        int remaining = product.getStockQuantity() - quantity;
        if (remaining < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product " + product.getId());
        }
        product.setStockQuantity(remaining);
        productRepository.save(product);
    }

    public BigDecimal priceFor(Product product) {
        return product.getPrice();
    }
}
