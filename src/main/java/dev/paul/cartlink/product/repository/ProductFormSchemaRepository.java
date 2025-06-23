package dev.paul.cartlink.product.repository;

import dev.paul.cartlink.product.model.ProductFormSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductFormSchemaRepository extends JpaRepository<ProductFormSchema, Long> {
    Optional<ProductFormSchema> findByProductType(String productType);
} 