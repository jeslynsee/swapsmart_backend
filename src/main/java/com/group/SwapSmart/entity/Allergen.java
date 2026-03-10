package com.group.SwapSmart.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "allergens")
public class Allergen {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Allergen name (e.g. "Gluten", "Peanuts", "Dairy")
    @Column(nullable = false, unique = true)
    private String name;

    // One allergen can belong to many user_allergens
    @OneToMany(mappedBy = "allergen")
    private List<UserAllergen> userAllergens;

    // Constructors
    public Allergen() {
    }

    public Allergen(String name) {
        this.name = name;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserAllergen> getUserAllergens() {
        return userAllergens;
    }

    public void setUserAllergens(List<UserAllergen> userAllergens) {
        this.userAllergens = userAllergens;
    }
}