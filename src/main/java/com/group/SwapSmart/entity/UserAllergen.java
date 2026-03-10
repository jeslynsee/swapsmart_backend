package com.group.SwapSmart.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_allergens")
public class UserAllergen {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to users table
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Foreign key to allergens table
    @ManyToOne
    @JoinColumn(name = "allergen_id", nullable = false)
    private Allergen allergen;

    // Constructors
    public UserAllergen() {
    }

    public UserAllergen(User user, Allergen allergen) {
        this.user = user;
        this.allergen = allergen;
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

    public Allergen getAllergen() {
        return allergen;
    }

    public void setAllergen(Allergen allergen) {
        this.allergen = allergen;
    }
}
