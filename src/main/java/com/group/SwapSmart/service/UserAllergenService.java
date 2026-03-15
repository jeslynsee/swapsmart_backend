package com.group.SwapSmart.service;

import com.group.SwapSmart.entity.Allergen;
import com.group.SwapSmart.entity.UserAllergen;
import com.group.SwapSmart.repository.AllergenRepository;
import com.group.SwapSmart.repository.UserAllergenRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAllergenService {

    private final UserAllergenRepository userAllergenRepository;
    private final AllergenRepository allergenRepository;

    // Constructor injection
    public UserAllergenService(UserAllergenRepository userAllergenRepository,
                               AllergenRepository allergenRepository) {
        this.userAllergenRepository = userAllergenRepository;
        this.allergenRepository = allergenRepository;
    }

    // Saves a list of allergen names for a user
    public void saveUserAllergens(String userId, List<String> allergenNames) {
        for (String allergenName : allergenNames) {

            // Strip "en:" prefix if it comes in from frontend
            String cleanName = allergenName.replace("en:", "").toLowerCase();

            // Look up allergen in DB by name
            Allergen allergen = allergenRepository.findByName(cleanName)
                    .orElseThrow(() -> new RuntimeException("Allergen not found: " + cleanName));

            // Create and save user allergen row
            UserAllergen userAllergen = new UserAllergen(userId, allergen);
            userAllergenRepository.save(userAllergen);
        }
    }

    // Gets all allergens for a user
    public List<UserAllergen> getUserAllergens(String userId) {
        return userAllergenRepository.findByUserId(userId);
    }
}