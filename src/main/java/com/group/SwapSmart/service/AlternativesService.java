package com.group.SwapSmart.service;

import com.group.SwapSmart.entity.Product;
import com.group.SwapSmart.model.ProductItem;
import com.group.SwapSmart.model.ProductResponse;
import com.group.SwapSmart.model.SearchResponse;
import com.group.SwapSmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AlternativesService {

    private final ProductRepository productsRepository;
    private final RestClient restClient;

    // Base URL for OpenFoodFacts API
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2";

    // Sugar threshold for alternatives search
    private static final double SUGAR_THRESHOLD = 2;

    // Constructor injection
    public AlternativesService(ProductRepository productsRepository) {
        this.productsRepository = productsRepository;
        this.restClient = RestClient.create();
    }

    // Main method: takes a barcode and returns a list of sugar free alternatives
    public List<Product> getAlternatives(String barcode) {

        // Step 1: Check if product is already cached in DB
        Optional<Product> cachedProduct = productsRepository.findByBarcode(barcode);

        String category;

        if (cachedProduct.isPresent()) {
            // Product already cached, grab category from DB instead of calling API
            category = cachedProduct.get().getCategory();
        } else {
            // Product not cached, call API to get product info
            ProductItem productItem = fetchProductByBarcode(barcode);

            if (productItem == null) { // if we got null from fetchProductByBarcode, then handled here by returning immutable list
                System.out.println("Could not grab item info from API, so cannot get alternatives");
                return List.of();
            }

            // grabbing the whole list of category tags here
            List<String> tags = productItem.getCategoryTags();
            System.out.println("Tags: " + tags); // for testing, need to delete

            //IMPORTANT: here, if no categories can be found, we just return immutable list; need to change logic to handle this scenario
            // null check in case category tags is empty or doesn't exist for product scanned
            if (tags == null || tags.isEmpty()) {
               System.out.println("No categories info available");
               return List.of();
            }

            // Grab most specific category tag from the API response (last item in list)
            category = tags.get(tags.size() - 1);

            // Save product to DB for future lookups
            saveProduct(productItem);
        }

        // Step 2: Search for sugar free alternatives in the same category
        return fetchAlternatives(category);
    }

   // Calls OpenFoodFacts API to get product info by barcode
    private ProductItem fetchProductByBarcode(String barcode) { 
        try {
            ProductResponse response = restClient.get()
            .uri(BASE_URL + "/product/" + barcode + "?fields=product_name,code,categories_tags_en,nutriments")
            .retrieve()
            .body(ProductResponse.class);

            if (response != null) { // null check here because RestClientException doesn't include null 
                return response.getProduct();
            } else {
                return null;
            }

        } catch (RestClientException e) {
            System.out.println("Error with calling API to get product data by barcode");
            return null;
        }
        
    }

    // Searches OpenFoodFacts for sugar free alternatives in the given category AND with the No Sugar label
    private List<Product> fetchAlternatives(String category) {  
        // Call search API with No Sugar label and category filters combined
        try {
            // URL encode category to handle spaces and special characters
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);
            SearchResponse response = restClient.get()
                .uri(BASE_URL + "/search?categories_tags_en=" + encodedCategory +
                "&labels_tags=en:no-sugar" +
                "&nutriments_sugars_100g_max=" + SUGAR_THRESHOLD +
                "&countries_tags=en:united-states" +
                "&fields=product_name,code,categories_tags_en,nutriments" +
                "&page_size=10")
                .retrieve()
                .body(SearchResponse.class);

            // null checking here because RestClientException doesn't include null
            if (response == null || response.getProducts() == null) { // checks if response null or if product list null
                return List.of();
            }

        // Secondary Java filter to catch any products that slipped through
        // then map to Product entity and return top 3
            return response.getProducts().stream()
                    .filter(item -> item.getNutriments() != null &&
                            item.getNutriments().getEffectiveSugars() != null &&
                            item.getNutriments().getEffectiveSugars() <= SUGAR_THRESHOLD)
                    .limit(3)
                    .map(this::mapToProduct)
                    .toList();
        } catch (RestClientException e) {
            System.out.println("Error with API data not matching up with our filtering/criteria. Returning empty list");
            return List.of();
        }
        
    }

        // Saves a new product to the DB
    private void saveProduct(ProductItem productItem) {
        Product product = mapToProduct(productItem);
        product.setLastChecked(LocalDateTime.now());
        productsRepository.save(product);
    }

    // Maps a ProductItem (API response) to a Products entity (DB)
    private Product mapToProduct(ProductItem item) {
        Product product = new Product();
        product.setBarcode(item.getBarcode());
        product.setProductName(item.getProductName());

        // Grab most specific category tag from the list (last item)
        // if (item.getCategoryTags() != null && !item.getCategoryTags().isEmpty()) {
        //     product.setCategory(item.getCategoryTags().get(item.getCategoryTags().size() - 1));
        // }

        if (item.getCategoryTags() != null && !item.getCategoryTags().isEmpty()) {
            List<String> tags = item.getCategoryTags();
            String chosen = tags.stream()
                    .filter(t -> t != null && t.startsWith("en:"))
                    .reduce((first, last) -> last)
                    .orElse(tags.get(tags.size() - 1));
            product.setCategory(chosen);
        }

        // Use effective sugars (added-sugars_100g if available, otherwise sugars_100g)
        if (item.getNutriments() != null) {
            product.setSugars100g(item.getNutriments().getEffectiveSugars());
        }

        product.setCountry("united-states");
        product.setLastChecked(LocalDateTime.now());
        return product;
    }
    //TODO: Look into FatSecret API, as OpenFoodFacts keeps producing unreliability issues
}
