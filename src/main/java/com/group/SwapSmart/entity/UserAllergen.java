package com.group.SwapSmart.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_allergens")
public class UserAllergen {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Supabase auth user UUID
    @Column(name = "user_id", nullable = false)
    private String userId;

    // Foreign key to allergens table
    @ManyToOne
    @JoinColumn(name = "allergen_id", nullable = false)
    private Allergen allergen;

    // Constructors
    public UserAllergen() {
    }

    public UserAllergen(String userId, Allergen allergen) {
        this.userId = userId;
        this.allergen = allergen;
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

    public Allergen getAllergen() {
        return allergen;
    }

    public void setAllergen(Allergen allergen) {
        this.allergen = allergen;
    }
}
