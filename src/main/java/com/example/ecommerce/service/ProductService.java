package com.example.ecommerce.service;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductImage;
import com.example.ecommerce.model.Role;
import com.example.ecommerce.repository.ProductImageRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.web.dto.ProductRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final StorageService storageService;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          StorageService storageService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.storageService = storageService;
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Transactional
    public Product createProduct(ProductRequest request, AppUser seller, List<MultipartFile> images) {
        if (seller.getRole() == Role.SELLER && !seller.isSellerVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller is not verified");
        }
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one product image is required");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSeller(seller);
        Product saved = productRepository.save(product);

        List<ProductImage> imageEntities = images.stream()
            .map(file -> {
                StoredFile storedFile = storageService.store(file);
                ProductImage image = new ProductImage();
                image.setProduct(saved);
                image.setStorageKey(storedFile.getKey());
                image.setOriginalFilename(storedFile.getOriginalFilename());
                image.setContentType(storedFile.getContentType());
                image.setSize(storedFile.getSize());
                image.setCreatedAt(Instant.now());
                return image;
            })
            .collect(Collectors.toList());
        productImageRepository.saveAll(imageEntities);

        return saved;
    }

    @Transactional
    public Product updateProduct(Long productId, ProductRequest request, AppUser seller, List<MultipartFile> images) {
        if (seller.getRole() == Role.SELLER && !seller.isSellerVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller is not verified");
        }
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one product image is required");
        }

        Product product = getProduct(productId);
        if (seller.getRole() == Role.SELLER) {
            if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update another seller's product");
            }
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        Product saved = productRepository.save(product);

        List<ProductImage> existingImages = productImageRepository.findByProductIdOrderByIdAsc(productId);
        for (ProductImage image : existingImages) {
            storageService.delete(image.getStorageKey());
        }
        productImageRepository.deleteAll(existingImages);

        List<ProductImage> imageEntities = images.stream()
            .map(file -> {
                StoredFile storedFile = storageService.store(file);
                ProductImage image = new ProductImage();
                image.setProduct(saved);
                image.setStorageKey(storedFile.getKey());
                image.setOriginalFilename(storedFile.getOriginalFilename());
                image.setContentType(storedFile.getContentType());
                image.setSize(storedFile.getSize());
                image.setCreatedAt(Instant.now());
                return image;
            })
            .collect(Collectors.toList());
        productImageRepository.saveAll(imageEntities);

        return saved;
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

    public List<ProductImage> listImages(Long productId) {
        return productImageRepository.findByProductIdOrderByIdAsc(productId);
    }

    public Map<Long, List<ProductImage>> listImagesByProductIds(List<Long> productIds) {
        List<ProductImage> images = productImageRepository.findByProductIdInOrderByIdAsc(productIds);
        return images.stream().collect(Collectors.groupingBy(image -> image.getProduct().getId()));
    }

    public ProductImage getImage(Long imageId) {
        return productImageRepository.findById(imageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product image not found"));
    }

    public Resource getImageResource(ProductImage image) {
        return storageService.loadAsResource(image.getStorageKey());
    }
}
