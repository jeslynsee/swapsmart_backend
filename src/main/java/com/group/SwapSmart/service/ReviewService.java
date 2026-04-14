package com.group.SwapSmart.service;

import com.group.SwapSmart.dto.ReviewDTO;
import com.group.SwapSmart.entity.Profile;
import com.group.SwapSmart.entity.Review;
import com.group.SwapSmart.repository.ProfileRepository;
import com.group.SwapSmart.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProfileRepository profileRepository;

    // Constructor injection
    public ReviewService(ReviewRepository reviewRepository,
                         ProfileRepository profileRepository) {
        this.reviewRepository = reviewRepository;
        this.profileRepository = profileRepository;
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

    // Gets all reviews for a specific product with username
    public List<ReviewDTO> getReviewsByBarcode(String barcode) {
        return reviewRepository.findByBarcode(barcode)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Gets all reviews across all products (community page)
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Gets all reviews by the authenticated user
    public List<ReviewDTO> getMyReviews(String userId) {
        return reviewRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Maps a Review entity to a ReviewDTO with username from profiles table
    private ReviewDTO mapToDTO(Review review) {
        String username = "Anonymous";
        String displayName = "Anonymous";
    
        var profileOpt = profileRepository.findById(review.getUserId());
        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            username = profile.getUsername();
            displayName = profile.getFirstName() + " " + profile.getLastName().charAt(0) + ".";
        }
    
        return new ReviewDTO(
                review.getId(),
                displayName,
                username,
                review.getBarcode(),
                review.getRating(),
                review.getComment(),
                review.getTimeCreated()
        );
    }
}