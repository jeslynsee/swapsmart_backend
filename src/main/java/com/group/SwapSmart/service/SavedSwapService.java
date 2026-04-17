package com.group.SwapSmart.service;

import com.group.SwapSmart.entity.Product;
import com.group.SwapSmart.entity.SavedSwap;
import com.group.SwapSmart.repository.ProductRepository;
import com.group.SwapSmart.repository.SavedSwapRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SavedSwapService {

    private final SavedSwapRepository savedSwapRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    // Constructor injection
    public SavedSwapService(SavedSwapRepository savedSwapRepository,
                            ProductRepository productRepository, ProductService productService) {
        this.savedSwapRepository = savedSwapRepository;
        this.productRepository = productRepository;
        this.productService = productService;
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
        Product alternativeProduct = productService.getOrFetchProduct(alternativeBarcode);

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
    
}