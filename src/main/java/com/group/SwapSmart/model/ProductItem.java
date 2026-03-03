package com.group.SwapSmart.model;
/*
This is a plain Java class representing the API response data from Open Food Facts. We are using this
as the interface between grabbing JSON data from API and storing it in Product table.
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductItem {

    // Product barcode
    @JsonProperty("code")
    private String barcode;

    // Product name
    @JsonProperty("product_name")
    private String productName;

    // Categories list from API
    @JsonProperty("categories_tags_en")
    private List<String> categoryTags;

    // Nutriments object from API
    @JsonProperty("nutriments")
    private Nutriments nutriments;

    // Nested class to capture sugar values from nutriments object
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nutriments {

        // Preferred field
        @JsonProperty("added-sugars_100g")
        private Double addedSugars100g;

        // Fallback field if added_sugars_100g is null
        @JsonProperty("sugars_100g")
        private Double sugars100g;

        // Returns added_sugars_100g if available, otherwise falls back to sugars_100g
        public Double getEffectiveSugars() {
            return addedSugars100g != null ? addedSugars100g : sugars100g;
        }

        public Double getAddedSugars100g() { return addedSugars100g; }
        public void setAddedSugars100g(Double addedSugars100g) { this.addedSugars100g = addedSugars100g; }

        public Double getSugars100g() { return sugars100g; }
        public void setSugars100g(Double sugars100g) { this.sugars100g = sugars100g; }
    }

    // Getters and Setters
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public List<String> getCategoryTags() { return categoryTags; }
    public void setCategoryTags(List<String> categoryTags) { this.categoryTags = categoryTags; }

    public Nutriments getNutriments() { return nutriments; }
    public void setNutriments(Nutriments nutriments) { this.nutriments = nutriments; }
}
