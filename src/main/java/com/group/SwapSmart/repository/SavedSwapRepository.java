package com.group.SwapSmart.repository;

import com.group.SwapSmart.entity.SavedSwap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedSwapRepository extends JpaRepository<SavedSwap, Long> {

    // Find all saved swaps for a specific user
    List<SavedSwap> findByUserId(String userId);

    // Check if a user already saved this specific swap
    // method name has to be formatted this way because Spring Data JPA will auto convert to query and look for these fields
    boolean existsByUserIdAndOriginalProduct_BarcodeAndAlternativeProduct_Barcode(
        String userId, String originalBarcode, String alternativeBarcode);
}