package com.group.SwapSmart.repository;

import com.group.SwapSmart.entity.UserAllergen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAllergenRepository extends JpaRepository<UserAllergen, Long> {

    // Find all allergens for a specific user
    List<UserAllergen> findByUserId(String userId);
}