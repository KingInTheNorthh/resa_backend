package com.example.ecommerce.repository;

import com.example.ecommerce.model.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
}
