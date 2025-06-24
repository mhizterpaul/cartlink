package dev.paul.cartlink.cart.dto;

import dev.paul.cartlink.cart.model.Cart;
import java.util.List;
import java.util.stream.Collectors;

public class CartResponse {
    private List<Item> items;

    public CartResponse() {
    }

    public CartResponse(Cart cart) {
        this.items = cart.getItems().stream().map(item -> {
            Item dto = new Item();
            dto.setProductId(item.getProduct() != null ? String.valueOf(item.getProduct().getId()) : null);
            dto.setQuantity(item.getQuantity());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {
        private String productId;
        private int quantity;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}