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

    // Foreign key to users table
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    public SavedSwap(User user, Product originalProduct, Product alternativeProduct, Double sugarDifference) {
        this.user = user;
        this.originalProduct = originalProduct;
        this.alternativeProduct = alternativeProduct;
        this.sugarDifference = sugarDifference;
        this.savedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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