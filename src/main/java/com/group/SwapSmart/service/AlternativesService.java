package com.group.SwapSmart.service;

import com.group.SwapSmart.entity.Product;
import com.group.SwapSmart.model.ProductItem;
import com.group.SwapSmart.model.ProductResponse;
import com.group.SwapSmart.model.SearchResponse;
import com.group.SwapSmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AlternativesService {

    private final ProductRepository productsRepository;
    private final RestClient restClient;

    // Base URL for OpenFoodFacts API
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2";

    // Sugar threshold for alternatives search
    private static final double SUGAR_THRESHOLD = 5;

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
        List<Product> alternatives;
        ProductItem productItem;
        List<String> tags;

        if (cachedProduct.isPresent()) {
            // Product already cached, grab category from DB instead of calling API
            category = cachedProduct.get().getCategory();
            // alternatives = fetchAlternativesWithLabel(category);
        } else {
            // Product not cached, call API to get product info
            productItem = fetchProductByBarcode(barcode);

            if (productItem == null) { // if we got null from fetchProductByBarcode, then handled here by returning immutable list
                System.out.println("Could not grab item info from API, so cannot get alternatives");
                return List.of();
            }

            // grabbing the whole list of category tags here
            tags = productItem.getCategoryTags();

            //if no categories can be found, we just return immutable list; need to change logic to handle this scenario
            // maybe look into other related tags
            // null check in case category tags is empty or doesn't exist for product scanned
            if (tags == null || tags.isEmpty()) {
               System.out.println("No categories info available");
               return List.of();
            }

            // Step 2: Search for sugar free alternatives in the same category

            // Grab most specific category tag from the API response (last item in list)
            category = tags.get(tags.size() - 1);

            // Trying for category hierarchy to get more results below
    
            // searching for alternatives first with most specific/last category
            alternatives = fetchAlternativesWithLabel(category);

            if (alternatives.isEmpty() && tags.size() > 1) { // if we get empty list (AKA no alts), and there is >1 category, check second to last category
                category = tags.get(tags.size() - 2); 
                alternatives = fetchAlternativesWithLabel(category);
            } 

            if (alternatives.isEmpty()) {
                alternatives = fetchAlternativesBySugar(category);
            }

            // Save product to DB for future lookups
            saveProduct(productItem, category);

            // returning alts found from first time barcode scan
            return alternatives;
        }

         // this is if we grab category from db cache, so we don't need to do any category work. just grab cached category
        // and try with no added sugar label. if no results, try by sugar

        alternatives = fetchAlternativesWithLabel(category);

        if (alternatives.isEmpty()) {
            alternatives = fetchAlternativesBySugar(category);
        }
       
        // returning alts from cached product
        return alternatives;
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
            System.out.println("Error with calling API to get product data by barcode " + e.getMessage());
            return null;
        }
        
    }

    // Searches OpenFoodFacts for sugar free alternatives in the given category AND with the No Added Sugar label
    private List<Product> fetchAlternativesWithLabel(String category) {
        try {
            // URL encode category to handle spaces and special characters; also replacing the + in HTML style encoding to %20 
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8).replace("+", "%20");
    
            // Build URL with encoded special characters to prevent URL parsing issues
            String url = BASE_URL + "/search" +
                "?categories_tags_en=" + encodedCategory +
                "&labels_tags=en:no-added-sugars" +
                "&nutriments_sugars_100g_max=" + SUGAR_THRESHOLD +
                "&countries_tags=en:united-states" +    
                "&fields=product_name,code,categories_tags_en,nutriments" +
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
                    .sorted(Comparator.comparingDouble(item -> item.getNutriments().getEffectiveSugars()))
                    .limit(3)
                    .map(this::mapToProduct)
                    .toList();
    
        } catch (RestClientException e) {
            // Log actual error message for debugging
            System.out.println("RestClient error: " + e.getMessage());
            return List.of();
        }
    }

    private  List<Product> fetchAlternativesBySugar(String category) {
        try {
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8).replace("+", "%20");

            String url = BASE_URL + "/search" +
            "?categories_tags_en=" + encodedCategory +
            "&nutriments_sugars_100g_max=" + SUGAR_THRESHOLD +
            "&countries_tags=en:united-states" +
            "&fields=product_name,code,categories_tags_en,nutriments" +
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
                        item.getNutriments().getEffectiveSugars() <= SUGAR_THRESHOLD)
                    .sorted(Comparator.comparingDouble(item -> item.getNutriments().getEffectiveSugars()))
                    .limit(3)
                    .map(this::mapToProduct)
                    .toList();

        } catch (RestClientException e) {
            System.out.println("Error fetching data with API Call for fetchAlternativesBySugar: " + e.getMessage());
            return List.of();
        }
        
    }

        // Saves a new product to the DB
    private void saveProduct(ProductItem productItem, String category) {
        Product product = mapToProduct(productItem, category);
        product.setLastChecked(LocalDateTime.now());
        productsRepository.save(product);
    }

    // Maps a ProductItem (API response) to a Products entity (DB)
    private Product mapToProduct(ProductItem item, String category) {
        Product product = new Product();
        product.setBarcode(item.getBarcode());
        product.setProductName(item.getProductName());
        product.setCategory(category);

        // Use effective sugars (added-sugars_100g if available, otherwise sugars_100g)
        if (item.getNutriments() != null) {
            product.setSugars100g(item.getNutriments().getEffectiveSugars());
        }

        product.setCountry("united-states");
        product.setLastChecked(LocalDateTime.now());
        return product;
    }

    // Used for mapping alternatives returned from search (not saved to DB)
    private Product mapToProduct(ProductItem item) {
        return mapToProduct(item, null);
    }
   
}
