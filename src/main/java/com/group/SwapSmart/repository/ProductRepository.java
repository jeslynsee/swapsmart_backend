package com.group.SwapSmart.repository;

import com.group.SwapSmart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find a product by barcode to check if it's already cached
    Optional<Product> findByBarcode(String barcode);

}
