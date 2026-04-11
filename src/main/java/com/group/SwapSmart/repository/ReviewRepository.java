package com.group.SwapSmart.repository;

import com.group.SwapSmart.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Get all reviews for a specific product by barcode
    List<Review> findByBarcode(String barcode);

    // Get all reviews by a specific user
    List<Review> findByUserId(String userId);

    // Check if user already reviewed this product
    boolean existsByUserIdAndBarcode(String userId, String barcode);
}