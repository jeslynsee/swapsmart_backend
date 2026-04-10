package com.group.SwapSmart.controller;

import com.group.SwapSmart.entity.SavedSwap;
import com.group.SwapSmart.service.SavedSwapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/swaps")
public class SavedSwapController {

    private final SavedSwapService savedSwapService;

    // Constructor injection
    public SavedSwapController(SavedSwapService savedSwapService) {
        this.savedSwapService = savedSwapService;
    }

    // Saves a swap for the authenticated user
    @PostMapping("/save")
    public ResponseEntity<?> saveSwap(Authentication authentication,
                                       @RequestBody Map<String, String> request) {
        try {
            String userId = authentication.getPrincipal().toString();
            String originalBarcode = request.get("originalBarcode");
            String alternativeBarcode = request.get("alternativeBarcode");

            // Validate request body
            if (originalBarcode == null || alternativeBarcode == null) {
                return ResponseEntity.badRequest().body("original barcode and alternative barcode are required");
            }

            SavedSwap savedSwap = savedSwapService.saveSwap(userId, originalBarcode, alternativeBarcode);
            return ResponseEntity.ok(savedSwap);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Gets all saved swaps for the authenticated user
    // since we use ManyToOne in join column, JPA auto gets full product info, so we can display relevant info 
    // in frontend Saved Swaps page
    @GetMapping
    public ResponseEntity<List<SavedSwap>> getSavedSwaps(Authentication authentication) {
        String userId = authentication.getPrincipal().toString();
        List<SavedSwap> savedSwaps = savedSwapService.getSavedSwaps(userId);
        return ResponseEntity.ok(savedSwaps);
    }
}