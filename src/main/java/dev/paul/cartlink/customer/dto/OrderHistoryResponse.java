package dev.paul.cartlink.customer.dto;

import java.util.List;
import java.time.LocalDateTime;

public class OrderHistoryResponse {
    private List<OrderDto> orders;

    public List<OrderDto> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDto> orders) {
        this.orders = orders;
    }

    public static class OrderDto {
        private Long id;
        private String productName;
        private int quantity;
        private double totalPrice;
        private LocalDateTime orderDate;
        private String status;

        public OrderDto() {
        }

        public OrderDto(Long id, String productName, int quantity, double totalPrice, LocalDateTime orderDate,
                String status) {
            this.id = id;
            this.productName = productName;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.orderDate = orderDate;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public LocalDateTime getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(LocalDateTime orderDate) {
            this.orderDate = orderDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}