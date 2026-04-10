package com.group.SwapSmart.service;

import com.group.SwapSmart.entity.Product;
import com.group.SwapSmart.entity.SavedSwap;
import com.group.SwapSmart.model.ProductItem;
import com.group.SwapSmart.model.ProductResponse;
import com.group.SwapSmart.repository.ProductRepository;
import com.group.SwapSmart.repository.SavedSwapRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SavedSwapService {

    private final SavedSwapRepository savedSwapRepository;
    private final ProductRepository productRepository;
    private final RestClient restClient;

    // Base URL for OpenFoodFacts API
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2";

    // Constructor injection
    public SavedSwapService(SavedSwapRepository savedSwapRepository,
                            ProductRepository productRepository) {
        this.savedSwapRepository = savedSwapRepository;
        this.productRepository = productRepository;
        this.restClient = RestClient.create();
    }

    // Saves a swap for the authenticated user
    public SavedSwap saveSwap(String userId, String originalBarcode, String alternativeBarcode) {

        // Check if user already saved this swap
        if (savedSwapRepository.existsByUserIdAndOriginalProduct_BarcodeAndAlternativeProduct_Barcode(
                userId, originalBarcode, alternativeBarcode)) {
            throw new RuntimeException("Swap already saved!");
        }

        // Look up original product from DB (should always be cached)
        Product originalProduct = productRepository.findByBarcode(originalBarcode)
                .orElseThrow(() -> new RuntimeException("Original product not found: " + originalBarcode));

        // Look up alternative product from DB — if not cached, fetch from OFF and save
        Product alternativeProduct = productRepository.findByBarcode(alternativeBarcode)
                .orElseGet(() -> fetchAndSaveProduct(alternativeBarcode));

        if (alternativeProduct == null) {
            throw new RuntimeException("Alternative product not found: " + alternativeBarcode);
        }

        // Calculate sugar difference
        Double sugarDifference = null;
        if (originalProduct.getSugars100g() != null && alternativeProduct.getSugars100g() != null) {
            sugarDifference = originalProduct.getSugars100g() - alternativeProduct.getSugars100g();
        }

        // Create and save the swap
        SavedSwap savedSwap = new SavedSwap(userId, originalProduct, alternativeProduct, sugarDifference);
        savedSwap.setSavedAt(LocalDateTime.now());

        return savedSwapRepository.save(savedSwap);
    }

    // Gets all saved swaps for the authenticated user
    public List<SavedSwap> getSavedSwaps(String userId) {
        return savedSwapRepository.findByUserId(userId);
    }

    // TODO: repeating code here; may refactor in the future -> create ProductService.java to have OFF calls to map product to DB
    // -> call productService from this file and AlternativesService file

    // Fetches product from OFF API and saves to DB
    private Product fetchAndSaveProduct(String barcode) {
        try {
            ProductResponse response = restClient.get()
                    .uri(BASE_URL + "/product/" + barcode + "?fields=product_name,code,categories_tags_en,nutriments,allergens_tags")
                    .retrieve()
                    .body(ProductResponse.class);

            if (response == null || response.getProduct() == null) {
                return null;
            }

            ProductItem productItem = response.getProduct();

            // Map to Product entity and save to DB
            Product product = new Product();
            product.setBarcode(productItem.getBarcode());
            product.setProductName(productItem.getProductName());

            // Grab most specific category if available
            if (productItem.getCategoryTags() != null && !productItem.getCategoryTags().isEmpty()) {
                product.setCategory(productItem.getCategoryTags().get(productItem.getCategoryTags().size() - 1));
            }

            // Set sugars
            if (productItem.getNutriments() != null) {
                product.setSugars100g(productItem.getNutriments().getEffectiveSugars());
            }

            // Set allergens
            if (productItem.getAllergensTags() != null && !productItem.getAllergensTags().isEmpty()) {
                product.setAllergens(String.join(",", productItem.getAllergensTags()));
            }

            product.setCountry("united-states");
            product.setLastChecked(LocalDateTime.now());

            return productRepository.save(product);

        } catch (RestClientException e) {
            System.out.println("Error fetching alternative product from OFF: " + e.getMessage());
            return null;
        }
    }
}