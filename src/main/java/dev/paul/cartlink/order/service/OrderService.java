package dev.paul.cartlink.order.service;

import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.merchant.dto.MerchantProduct;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.model.*;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.model.OrderStatus;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.product.model.ProductLink;
import dev.paul.cartlink.product.service.ProductLinkService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductLinkService productLinkService;

    public OrderService(OrderRepository orderRepository,
            ProductLinkService productLinkService) {
        this.orderRepository = orderRepository;
        this.productLinkService = productLinkService;
    }

    @Transactional
    public Order createOrder(MerchantProduct merchantProduct, Customer customer,
            Integer orderSize, ProductLink productLink) {
        if (merchantProduct.getStock() < orderSize) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        Order order = new Order();
        order.setMerchantProduct(merchantProduct);
        order.setCustomer(customer);
        order.setOrderSize(orderSize);
        order.setStatus(OrderStatus.PENDING);
        order.setPaid(false);
        order.setProductLink(productLink);

        Order savedOrder = orderRepository.save(order);

        if (productLink != null) {
            productLinkService.trackConversion(productLink);
        }

        return savedOrder;
    }

    public List<Order> getMerchantOrders(Merchant merchant) {
        return orderRepository.findByMerchantProduct_Merchant(merchant);
    }

    public List<Order> getMerchantOrdersByStatus(Merchant merchant, OrderStatus status) {
        return orderRepository.findByMerchantProduct_MerchantAndStatus(merchant, status);
    }

    public List<Order> getMerchantOrdersByDateRange(Merchant merchant, LocalDateTime startDate,
            LocalDateTime endDate) {
        return orderRepository.findByMerchantProduct_MerchantAndCreatedAtBetween(merchant, startDate, endDate);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public void updateOrderTracking(Long orderId, String trackingId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setTrackingId(trackingId);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    public List<Order> getOrdersByProductLink(Long linkId) {
        return orderRepository.findByProductLink_LinkId(linkId);
    }
}