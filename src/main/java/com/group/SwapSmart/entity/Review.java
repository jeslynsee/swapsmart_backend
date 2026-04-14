package com.group.SwapSmart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to Supabase Auth User table
    @Column(name = "user_id", nullable = false)
    private String userId;

    // Barcode of product user is reviewing (no longer constraining to a saved swap)
    @Column(nullable = false)
    private String barcode;

    // Rating (e.g. 1-5)
    @Column(nullable = false)
    private Integer rating;

    // Optional comment
    @Column(length = 1000)
    private String comment;

    // Timestamp of when review was created
    @Column
    private LocalDateTime timeCreated;

    // Constructors
    public Review() {
    }

    public Review(String userId, String barcode, Integer rating, String comment) {
        this.userId = userId;
        this.barcode = barcode;
        this.rating = rating;
        this.comment = comment;
        this.timeCreated = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(LocalDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }
}
