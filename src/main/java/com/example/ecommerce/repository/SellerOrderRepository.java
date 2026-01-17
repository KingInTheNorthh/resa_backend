package com.example.ecommerce.repository;

import com.example.ecommerce.model.SellerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, Long> {
}
