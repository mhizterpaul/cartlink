package dev.paul.cartlink.product.dto;

import java.util.List;
import java.util.Map;

public class ProductEditRequest {
    private Long productId;
    private String name;
    private String brand;
    private String category;

    public ProductEditRequest() {
    }

    public ProductEditRequest(Long productId, String name, String brand, String category) {
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.category = category;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}