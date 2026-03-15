package com.group.SwapSmart.repository;

import com.group.SwapSmart.entity.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {

    // Find allergen by name (e.g. "gluten", "milk")
    Optional<Allergen> findByName(String name);
}