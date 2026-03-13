package com.group.SwapSmart.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.group.SwapSmart.entity.Profile;
import com.group.SwapSmart.repository.ProfileRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ProfileRepository profileRepository;

    public UserController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
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

}