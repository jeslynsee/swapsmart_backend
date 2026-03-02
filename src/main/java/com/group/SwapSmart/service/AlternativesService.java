package com.group.SwapSmart.service;

import com.group.SwapSmart.entity.Product;
import com.group.SwapSmart.model.ProductItem;
import com.group.SwapSmart.model.ProductResponse;
import com.group.SwapSmart.model.SearchResponse;
import com.group.SwapSmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
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
    private static final double SUGAR_THRESHOLD = 20;

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

            // Grab most specific category tag from the API response (last item in list)
            category = productItem.getCategoryTags().get(productItem.getCategoryTags().size() - 1);

            // Save product to DB for future lookups
            saveProduct(productItem);
        }

        // Step 2: Search for sugar free alternatives in the same category
        return fetchAlternatives(category);
    }

   // Calls OpenFoodFacts API to get product info by barcode
    private ProductItem fetchProductByBarcode(String barcode) { 
        ProductResponse response = restClient.get()
            .uri(BASE_URL + "/product/" + barcode + "?fields=product_name,code,categories_tags_en,nutriments")
            .retrieve()
            .body(ProductResponse.class);

        return response.getProduct();
    }

    // Searches OpenFoodFacts for sugar free alternatives in the given category
    private List<Product> fetchAlternatives(String category) {
        // Call search API with category and sugar threshold filters
        SearchResponse response = restClient.get()
            .uri(BASE_URL + "/search?categories_tags_en=" + category +
                    "&nutriments_sugars_100g_max=" + SUGAR_THRESHOLD +
                    "&countries_tags_en=united-states" +
                    "&fields=product_name,code,categories_tags_en,nutriments" +
                    "&page_size=10")
            .retrieve()
            .body(SearchResponse.class);

    // Secondary Java filter to catch any products that slipped through the API filter
    // then map to Product entity and return top 3
        return response.getProducts().stream()
            .filter(item -> item.getNutriments() != null &&
                    item.getNutriments().getEffectiveSugars() != null &&
                    item.getNutriments().getEffectiveSugars() <= SUGAR_THRESHOLD)
            .limit(3)
            .map(this::mapToProduct)
            .toList();
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
        if (item.getCategoryTags() != null && !item.getCategoryTags().isEmpty()) {
            product.setCategory(item.getCategoryTags().get(item.getCategoryTags().size() - 1));
        }

        // Use effective sugars (added_sugars_100g if available, otherwise sugars_100g)
        if (item.getNutriments() != null) {
            product.setSugars100g(item.getNutriments().getEffectiveSugars());
        }

        product.setCountry("united-states");
        product.setLastChecked(LocalDateTime.now());
        return product;
    }
}
