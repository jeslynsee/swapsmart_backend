package com.group.SwapSmart.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.group.SwapSmart.entity.Profile;
import com.group.SwapSmart.entity.UserAllergen;
import com.group.SwapSmart.repository.ProfileRepository;
import com.group.SwapSmart.service.UserAllergenService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ProfileRepository profileRepository;
    private final UserAllergenService userAllergenService;

    public UserController(ProfileRepository profileRepository,
                          UserAllergenService userAllergenService) {
        this.profileRepository = profileRepository;
        this.userAllergenService = userAllergenService;
    }

    // need to save user details to profile table
    // Creates a profile for a newly registered user; request body auto maps user's first name, last name, and username
    @PostMapping("/profile")
    public Profile createProfile(Authentication authentication, @RequestBody Profile profile) {
        // Set the profile ID to the authenticated user's Supabase UUID
        String userId = authentication.getPrincipal().toString();
        profile.setId(userId);
        return profileRepository.save(profile);
    }

   // Returns current authenticated user's ID from JWT
   @GetMapping("/me")
   public String getCurrentUser(Authentication authentication) {
       return authentication.getPrincipal().toString();
   }

   // Saves allergens for the authenticated user
    @PostMapping("/allergens")
    public void saveUserAllergens(Authentication authentication, @RequestBody List<String> allergenNames) {
        String userId = authentication.getPrincipal().toString();
        userAllergenService.saveUserAllergens(userId, allergenNames);
    }

    // Gets all allergens for the authenticated user
    @GetMapping("/allergens")
    public List<UserAllergen> getUserAllergens(Authentication authentication) {
        String userId = authentication.getPrincipal().toString();
        return userAllergenService.getUserAllergens(userId);
    }

}