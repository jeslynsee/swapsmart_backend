package com.group.SwapSmart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Wrapper class to map the OpenFoodFacts single product response
// The API returns product data nested inside a "product" object
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse {

    // Maps to the "product" object in the API response
    @JsonProperty("product")
    private ProductItem product;

    public ProductItem getProduct() { return product; }
    public void setProduct(ProductItem product) { this.product = product; }
}