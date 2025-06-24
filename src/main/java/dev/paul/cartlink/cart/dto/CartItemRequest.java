package dev.paul.cartlink.cart.dto;

public class CartItemRequest {
    private Long merchantProductId;
    private int quantity;

    public CartItemRequest() {
    }

    public CartItemRequest(Long productId, int quantity) {
        this.merchantProductId = productId;
        this.quantity = quantity;
    }

    public Long getMerchantProductId() {
        return merchantProductId;
    }

    public void setProductId(Long merchantProductId) {
        this.merchantProductId = merchantProductId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}