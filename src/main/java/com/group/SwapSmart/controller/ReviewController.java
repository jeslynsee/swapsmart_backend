package com.group.SwapSmart.controller;

import com.group.SwapSmart.entity.Review;
import com.group.SwapSmart.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // Constructor injection
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // POST /api/reviews/{barcode}
    // Creates a review for a product
    @PostMapping("/{barcode}")
    public ResponseEntity<?> createReview(Authentication authentication,
                                           @PathVariable String barcode,
                                           @RequestBody Map<String, Object> request) {
        try {
            String userId = authentication.getPrincipal().toString();
            Integer rating = (Integer) request.get("rating");
            String comment = (String) request.get("comment");

            if (rating == null) {
                return ResponseEntity.badRequest().body("Rating is required");
            }

            Review review = reviewService.createReview(userId, barcode, rating, comment);
            return ResponseEntity.ok(review);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /api/reviews/{barcode}
    // Gets all reviews for a specific product
    @GetMapping("/{barcode}")
    public ResponseEntity<List<Review>> getReviewsByBarcode(@PathVariable String barcode) {
        List<Review> reviews = reviewService.getReviewsByBarcode(barcode);
        return ResponseEntity.ok(reviews);
    }

    // GET /api/reviews
    // Gets all reviews across all products (community page)
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // GET /api/reviews/mine
    // Gets all reviews by the authenticated user
    @GetMapping("/mine")
    public ResponseEntity<List<Review>> getMyReviews(Authentication authentication) {
        String userId = authentication.getPrincipal().toString();
        List<Review> reviews = reviewService.getMyReviews(userId);
        return ResponseEntity.ok(reviews);
    }
}