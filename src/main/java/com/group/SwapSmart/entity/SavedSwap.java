package com.group.SwapSmart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_swaps")
public class SavedSwap {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Refrencing Supabase Auth User table
    @Column(name = "user_id", nullable = false)
    private String userId;

    // Foreign key to products table (original scanned product)
    @ManyToOne
    @JoinColumn(name = "original_product_id", nullable = false)
    private Product originalProduct;

    // Foreign key to products table (alternative product)
    @ManyToOne
    @JoinColumn(name = "alternative_product_id", nullable = false)
    private Product alternativeProduct;

    // Sugar difference between original and alternative product
    @Column
    private Double sugarDifference;

    // Timestamp of when swap was saved
    @Column
    private LocalDateTime savedAt;

    // Constructors
    public SavedSwap() {
    }

    public SavedSwap(String userId, Product originalProduct, Product alternativeProduct, Double sugarDifference) {
        this.userId = userId;
        this.originalProduct = originalProduct;
        this.alternativeProduct = alternativeProduct;
        this.sugarDifference = sugarDifference;
        this.savedAt = LocalDateTime.now();
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

    public Product getOriginalProduct() {
        return originalProduct;
    }

    public void setOriginalProduct(Product originalProduct) {
        this.originalProduct = originalProduct;
    }

    public Product getAlternativeProduct() {
        return alternativeProduct;
    }

    public void setAlternativeProduct(Product alternativeProduct) {
        this.alternativeProduct = alternativeProduct;
    }

    public Double getSugarDifference() {
        return sugarDifference;
    }

    public void setSugarDifference(Double sugarDifference) {
        this.sugarDifference = sugarDifference;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
}