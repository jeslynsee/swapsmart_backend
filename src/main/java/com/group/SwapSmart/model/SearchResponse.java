package com.group.SwapSmart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/*
 Wrapper class to map the OpenFoodFacts search response.
 The API returns results nested inside a "products" array so we need
 this class to extract the list before passing it to the service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {

    // Maps to the "products" array in the API response
    @JsonProperty("products")
    private List<ProductItem> products;

    public List<ProductItem> getProducts() { return products; }
    public void setProducts(List<ProductItem> products) { this.products = products; }
}