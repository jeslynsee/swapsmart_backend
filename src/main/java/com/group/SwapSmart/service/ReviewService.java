package com.group.SwapSmart.service;

import com.group.SwapSmart.entity.Review;
import com.group.SwapSmart.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // Constructor injection
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Creates a review for a product
    public Review createReview(String userId, String barcode, Integer rating, String comment) {

        // Check if user already reviewed this product
        if (reviewRepository.existsByUserIdAndBarcode(userId, barcode)) {
            throw new RuntimeException("You have already reviewed this product!");
        }

        // Validate rating range
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5!");
        }

        // Create and save review
        Review review = new Review(userId, barcode, rating, comment);
        review.setTimeCreated(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    // Gets all reviews for a specific product
    public List<Review> getReviewsByBarcode(String barcode) {
        return reviewRepository.findByBarcode(barcode);
    }

    // Gets all reviews across all products (community page)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Gets all reviews by the authenticated user
    public List<Review> getMyReviews(String userId) {
        return reviewRepository.findByUserId(userId);
    }
}