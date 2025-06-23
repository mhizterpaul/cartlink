package dev.paul.cartlink.product.dto;

import lombok.Data;

@Data
public class ProductCreateRequest {

    private String category;
    private String productType;
    private String brand;
    private String name;
    private String description;

    public ProductCreateRequest() {
    }

    public ProductCreateRequest(String category, String productType, String brand, String name, String description){
        this.category = category;
        this.productType = productType;
        this.brand = brand;
        this.name = name;
        this.description = description;
    }

}
