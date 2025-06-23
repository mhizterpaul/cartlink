package dev.paul.cartlink.cart.dto;

public class CartItemUpdateRequest {
    private int quantity;

    public CartItemUpdateRequest() {
    }

    public CartItemUpdateRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}