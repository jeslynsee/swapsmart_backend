package com.group.SwapSmart.service;

import com.group.SwapSmart.entity.Product;
import com.group.SwapSmart.model.ProductItem;
import com.group.SwapSmart.model.SearchResponse;
import com.group.SwapSmart.repository.ProductRepository;
import com.group.SwapSmart.repository.UserAllergenRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AlternativesService {

    private final ProductRepository productsRepository;
    private final UserAllergenRepository userAllergenRepository;
    private final RestClient restClient;
    private final ProductService productService;

    // Base URL for OpenFoodFacts API
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2";

    // Sugar threshold for alternatives search
    private static final double SUGAR_THRESHOLD = 5;

    // Constructor injection
    public AlternativesService(ProductRepository productsRepository, UserAllergenRepository userAllergenRepository, ProductService productService) {
        this.productsRepository = productsRepository;
        this.userAllergenRepository = userAllergenRepository;
        this.restClient = RestClient.create();
        this.productService = productService;
    }

    // Main method: takes a barcode and returns a list of sugar free alternatives
    public List<Product> getAlternatives(String barcode, String userId) {

        // Fetch user allergens if logged in, otherwise empty list
        List<String> userAllergens = new ArrayList<>();
        if (userId != null) {
            userAllergens = userAllergenRepository.findByUserId(userId)
                    .stream()
                    .map(ua -> ua.getAllergen().getName())
                    .toList();
            // System.out.println("User allergens: " + userAllergens); // debug statement
        }

        // Check if product is already cached in DB
        Optional<Product> cachedProduct = productsRepository.findByBarcode(barcode);

        String category;
        List<Product> alternatives;
        ProductItem productItem;
        List<String> tags;
        double sugarThreshold;

        if (cachedProduct.isPresent()) {
            // Product already cached, grab category from DB instead of calling API
            category = cachedProduct.get().getCategory();
            // grabbing sugar from db, but if null, set threshold to defined one in class
            sugarThreshold = cachedProduct.get().getSugars100g() != null
            ? cachedProduct.get().getSugars100g()
            : SUGAR_THRESHOLD;

            if (category == null || category.isBlank()) {
                return List.of();
            }

        } else {
            // Product not cached, call API to get product info
            productItem = productService.fetchProductByBarcode(barcode);

            if (productItem == null) { // if we got null from fetchProductByBarcode, then handled here by returning immutable list
                System.out.println("Could not grab item info from API, so cannot get alternatives");
                return List.of();
            }

            // grabbing the whole list of category tags here
            tags = productItem.getCategoryTags();
            // System.out.println("Category tags from OFF: " + tags);

            //if no categories can be found, we just return immutable list; need to change logic to handle this scenario
            // maybe look into other related tags
            // null check in case category tags is empty or doesn't exist for product scanned
            if (tags == null || tags.isEmpty()) {
               System.out.println("No categories info available");
               return List.of();
            }

            // Next: Search for sugar free alternatives in the same category

            // Grab most specific category tag from the API response (last item in list)
            category = tags.get(tags.size() - 1);
            // grab sugar_100g from product item and set as sugar threshold, or fallback to defined sugar threshold if null
            sugarThreshold = productItem.getNutriments() != null && productItem.getNutriments().getEffectiveSugars() != null
            ? productItem.getNutriments().getEffectiveSugars()
            : SUGAR_THRESHOLD;

            // Trying for category hierarchy to get more results below

            // searching for alternatives first with most specific/last category
            alternatives = fetchAlternativesWithLabel(category, userAllergens);

            if (alternatives.isEmpty() && tags.size() > 1) { // if we get empty list (AKA no alts), and there is >1 category, check second to last category
                category = tags.get(tags.size() - 2);
                alternatives = fetchAlternativesWithLabel(category, userAllergens);
            }

            if (alternatives.isEmpty()) {
                alternatives = fetchAlternativesBySugar(category, sugarThreshold, userAllergens);
            }

            // Save product to DB for future lookups
            productService.saveProduct(productItem, category);

            // returning alts found from first time barcode scan
            return alternatives;
        }

         // this is if we grab category from db cache, so we don't need to do any category work. just grab cached category
        // and try with no added sugar label. if no results, try by sugar

        alternatives = fetchAlternativesWithLabel(category, userAllergens);

        if (alternatives.isEmpty()) {
            alternatives = fetchAlternativesBySugar(category, sugarThreshold, userAllergens);
        }

        // returning alts from cached product
        return alternatives;
    }

    // Searches OpenFoodFacts for sugar free alternatives in the given category AND with the No Added Sugar label
    private List<Product> fetchAlternativesWithLabel(String category, List<String> userAllergens) {
        try {
            // URL encode category to handle spaces and special characters; also replacing the + in HTML style encoding to %20
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8).replace("+", "%20");

            // Build URL with encoded special characters to prevent URL parsing issues
            String url = BASE_URL + "/search" +
                "?categories_tags_en=" + encodedCategory +
                "&labels_tags=en:no-added-sugars" +
                "&nutriments_sugars_100g_max=" + SUGAR_THRESHOLD +
                "&countries_tags=en:united-states" +
                "&fields=product_name,code,categories_tags_en,nutriments,allergens_tags" +
                "&page_size=10";

            // System.out.println("Fetching alternatives with URL: " + url); // debug statement

            SearchResponse response = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(SearchResponse.class);

            // Null check here because RestClientException doesn't include null
            // Checks if response is null or if product list within response is null
            if (response == null || response.getProducts() == null) {
                return List.of();
            }

            // Secondary Java filter to catch any products that slipped through the API filter
            // then map to Product entity and return top 3
            return response.getProducts().stream()
                    .filter(item -> item.getNutriments() != null &&
                            item.getNutriments().getEffectiveSugars() != null &&
                            item.getNutriments().getEffectiveSugars() <= SUGAR_THRESHOLD)
                    .filter(item -> !containsUserAllergens(item, userAllergens))
                    .sorted(Comparator.comparingDouble(item -> item.getNutriments().getEffectiveSugars()))
                    .limit(3)
                    .map(item -> productService.mapToProduct(item, null))
                    .toList();

        } catch (RestClientException e) {
            // Log actual error message for debugging
            System.out.println("RestClient error: " + e.getMessage());
            return List.of();
        }
    }


    private  List<Product> fetchAlternativesBySugar(String category, double sugarThreshold, List<String> userAllergens) {
        try {
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8).replace("+", "%20");

            String url = BASE_URL + "/search" +
            "?categories_tags_en=" + encodedCategory +
            "&nutriments_sugars_100g_max=" + sugarThreshold +
            "&countries_tags=en:united-states" +
            "&fields=product_name,code,categories_tags_en,nutriments,allergens_tags" +
            "&page_size=10";

            SearchResponse response = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(SearchResponse.class);

            if (response == null || response.getProducts() == null) {
                return List.of();
            }

            return response.getProducts().stream()
                    .filter(item -> item.getNutriments() != null &&
                        item.getNutriments().getEffectiveSugars() != null &&
                        item.getNutriments().getEffectiveSugars() < sugarThreshold)
                    .filter(item -> !containsUserAllergens(item, userAllergens))
                    .sorted(Comparator.comparingDouble(item -> item.getNutriments().getEffectiveSugars()))
                    .limit(3)
                    .map(item -> productService.mapToProduct(item, null))
                    .toList();

        } catch (RestClientException e) {
            System.out.println("Error fetching data with API Call for fetchAlternativesBySugar: " + e.getMessage());
            return List.of();
        }

    }

    // Checks if a product contains any of the user's allergens
    // Returns true if product should be filtered out
    private boolean containsUserAllergens(ProductItem item, List<String> userAllergens) {
        // if no user allergens or product has no allergen data, don't filter out
        if (userAllergens.isEmpty() || item.getAllergensTags() == null) return false;

        return userAllergens.stream()
                .anyMatch(allergen -> item.getAllergensTags().contains("en:" + allergen));
    }


}