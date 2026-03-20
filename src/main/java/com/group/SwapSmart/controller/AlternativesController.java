package com.group.SwapSmart.controller;

import com.group.SwapSmart.entity.Product;
import com.group.SwapSmart.service.AlternativesService;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alts")
public class AlternativesController {

    private final AlternativesService alternativesService;

    // Constructor injection
    public AlternativesController(AlternativesService alternativesService) {
        this.alternativesService = alternativesService;
    }

    // GET /api/alts/{barcode}
    // Takes a barcode and returns a list of sugar free alternatives
    @GetMapping("/{barcode}")
    public ResponseEntity<List<Product>> getSugarFreeAlts(@PathVariable String barcode, Authentication authentication) {

        // Normalize barcode to 13 digits with leading zero if needed
        if (barcode.length() == 12) {
            barcode = "0" + barcode;
        }

        // Extract userId from JWT if logged in, otherwise null
        String userId = authentication != null
                ? authentication.getPrincipal().toString()
                : null;

        List<Product> alternatives = alternativesService.getAlternatives(barcode, userId);
        return ResponseEntity.ok(alternatives);
    }

}