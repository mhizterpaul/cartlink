package dev.paul.cartlink.product.dto;

import java.util.List;
import java.util.Map;

public class ProductEditRequest {
    private String name;
    private String model;
    private String manufacturer;
    private Integer stock;
    private Double price;
    private List<String> images;
    private Map<String, String> specifications;
    private String merchantId;
    private List<String> videos;

    public ProductEditRequest() {
    }

    public ProductEditRequest(String name, String model, String manufacturer, Integer stock, Double price,
            List<String> images, Map<String, String> specifications, String merchantId, List<String> videos) {
        this.name = name;
        this.model = model;
        this.manufacturer = manufacturer;
        this.stock = stock;
        this.price = price;
        this.images = images;
        this.specifications = specifications;
        this.merchantId = merchantId;
        this.videos = videos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Map<String, String> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Map<String, String> specifications) {
        this.specifications = specifications;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public List<String> getVideos() {
        return videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos;
    }
}