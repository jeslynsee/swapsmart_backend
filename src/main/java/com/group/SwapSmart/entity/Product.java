package com.group.SwapSmart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    // Barcode is unique since we use it to check if product is already cached
    @Column(unique = true, nullable = false)
    private String barcode;

    // Product name
    @Column
    private String productName;

    // Category used for alternatives search
    @Column
    private String category;

    // Sugar value per 100g
    @Column
    private Double sugars100g;

    // Country filter
    @Column
    private String country;

    // Timestamp of when product was first checked/scanned from API
    @Column
    private LocalDateTime lastChecked;

    // Allergens contained in this product e.g. "en:gluten,en:milk"
    @Column(length = 1000)
    private String allergens;

    // Getter and Setter
    public String getAllergens() { 
        return allergens; 
    }

    public void setAllergens(String allergens) { 
        this.allergens = allergens; 
    }

    // Getters and Setters
    public Long getProductId() { 
        return productId; 
    }

    public void setProductId(Long productId) { 
        this.productId = productId; 
    }

    public String getBarcode() { 
        return barcode; 
    }

    public void setBarcode(String barcode) { 
        this.barcode = barcode;
     }

    public String getProductName() { 
        return productName; 
    }

    public void setProductName(String productName) { 
        this.productName = productName; 
    }

    public String getCategory() { 
        return category; 
    }

    public void setCategory(String category) { 
        this.category = category; 
    }

    public Double getSugars100g() { 
        return sugars100g; 
    }

    public void setSugars100g(Double sugars100g) { 
        this.sugars100g = sugars100g; 
    }

    public String getCountry() { 
        return country; 
    }

    public void setCountry(String country) { 
        this.country = country; 
    }

    public LocalDateTime getLastChecked() { return lastChecked; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }
}
