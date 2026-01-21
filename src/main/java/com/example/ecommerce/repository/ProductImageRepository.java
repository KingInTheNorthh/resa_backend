package com.example.ecommerce.repository;

import com.example.ecommerce.model.ProductImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByIdAsc(Long productId);

    List<ProductImage> findByProductIdInOrderByIdAsc(List<Long> productIds);

    void deleteByProductId(Long productId);
}
