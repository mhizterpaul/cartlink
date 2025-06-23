package dev.paul.cartlink.product.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_form_schema")
public class ProductFormSchema {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productType;

    @Type(value = JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String schemaJson;

    @Column(nullable = false)
    private String source; // e.g., "LLM"

    @Column(nullable = false)
    private String status; // e.g., "active"

    @Column(columnDefinition = "TEXT")
    private String usageStatsJson; // JSON for usage stats, optional
} 