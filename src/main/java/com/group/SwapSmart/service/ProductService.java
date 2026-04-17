package com.group.SwapSmart.service;

import com.group.SwapSmart.entity.Product;
import com.group.SwapSmart.model.ProductItem;
import com.group.SwapSmart.model.ProductResponse;
import com.group.SwapSmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final RestClient restClient;

    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2";

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.restClient = RestClient.create();
    }

    // Fetch product from DB or OFF API, save if not cached
    public Product getOrFetchProduct(String barcode) {
        return productRepository.findByBarcode(barcode)
                .orElseGet(() -> fetchAndSaveProduct(barcode));
    }

    // Fetch product from OFF API by barcode
    public ProductItem fetchProductByBarcode(String barcode) {
        try {
            ProductResponse response = restClient.get()
                    .uri(BASE_URL + "/product/" + barcode + "?fields=product_name,code,categories_tags_en,nutriments,allergens_tags")
                    .retrieve()
                    .body(ProductResponse.class);

            if (response == null || response.getProduct() == null) {
                return null;
            }
            return response.getProduct();

        } catch (RestClientException e) {
            System.out.println("Error fetching product by barcode: " + e.getMessage());
            return null;
        }
    }

    // Fetch from OFF and save to DB; this function is a helper of getOrFetchProduct, which is used in SavedSwapService
    // Need to save the alternative product because user can save the swap/alt and it can be displayed on a saved swaps page
    public Product fetchAndSaveProduct(String barcode) {
        ProductItem productItem = fetchProductByBarcode(barcode);
        if (productItem == null) return null;

        String category = null;

        //below keeps our specific capture of the best category we can get of product, so we don't get empty list for alt results
        if (productItem.getCategoryTags() != null && !productItem.getCategoryTags().isEmpty()) {
            category = productItem.getCategoryTags().get(productItem.getCategoryTags().size() - 1);
        }

        return saveProduct(productItem, category);
    }

    // Map ProductItem to Product entity and save to DB
    public Product saveProduct(ProductItem productItem, String category) {
        Product product = mapToProduct(productItem, category);
        return productRepository.save(product);
    }

    // Maps a ProductItem to a Product entity
    public Product mapToProduct(ProductItem item, String category) {
        Product product = new Product();
        product.setBarcode(item.getBarcode());
        product.setProductName(item.getProductName());
        product.setCategory(category);

        if (item.getNutriments() != null) {
            product.setSugars100g(item.getNutriments().getEffectiveSugars());
        }

        if (item.getAllergensTags() != null && !item.getAllergensTags().isEmpty()) {
            product.setAllergens(String.join(",", item.getAllergensTags()));
        }

        product.setCountry("united-states");
        product.setLastChecked(LocalDateTime.now());
        return product;
    }
}