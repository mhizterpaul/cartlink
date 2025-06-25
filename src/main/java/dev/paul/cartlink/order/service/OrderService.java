package dev.paul.cartlink.order.service;

import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.model.OrderStatus;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.link.repository.LinkRepository;
import dev.paul.cartlink.link.model.Link;
import dev.paul.cartlink.link.service.LinkAnalyticsService;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final LinkRepository linkRepository;
    private final LinkAnalyticsService linkAnalyticsService;
    private final MerchantProductRepository merchantProductRepository;

    public OrderService(OrderRepository orderRepository,
            LinkRepository linkRepository, LinkAnalyticsService linkAnalyticsService,
            MerchantProductRepository merchantProductRepository) {
        this.orderRepository = orderRepository;
        this.linkRepository = linkRepository;
        this.linkAnalyticsService = linkAnalyticsService;
        this.merchantProductRepository = merchantProductRepository;
    }

    @Transactional
    public Order createOrder(MerchantProduct merchantProduct, Customer customer,
            Integer orderSize, Long linkId) {
        if (merchantProduct.getStock() < orderSize) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        // Fetch LinkAnalytics by analyticsId
        Link productLink = linkRepository.getReferenceById(linkId);

        Order order = new Order();
        order.setMerchantProduct(merchantProduct);
        order.setCustomer(customer);
        order.setOrderSize(orderSize);
        order.setStatus(OrderStatus.PENDING);
        order.setPaid(false);
        order.setLink(productLink);

        Order savedOrder = orderRepository.save(order);

        if (productLink != null) {
            linkAnalyticsService.updateTotalOrders(productLink);
        }

        return savedOrder;
    }

    // Create order for customer (guest or logged in)
    @Transactional
    public Order createOrderForCustomer(Customer customer, Order order) {
        order.setCustomer(customer);
        if (order.getStatus() == null)
            order.setStatus(OrderStatus.PENDING);
        if (order.getPaid() == null)
            order.setPaid(false);
        return orderRepository.save(order);
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

    public List<Order> getOrdersByProductLink(Long linkId) {
        return orderRepository.findByProductLink_LinkId(linkId);
    }

    // Add method for paginated customer order history
    public List<Order> getCustomerOrderHistory(Customer customer, int page, int limit) {
        // Use repository to fetch orders for customer, add pagination if needed
        // For now, fetch all and subList for demo
        List<Order> allOrders = orderRepository.findByCustomer(customer);
        int fromIndex = Math.max(0, (page - 1) * limit);
        int toIndex = Math.min(allOrders.size(), fromIndex + limit);
        if (fromIndex > toIndex)
            return List.of();
        return allOrders.subList(fromIndex, toIndex);
    }

    /**
     * Retrieves a Merchant by its ID by searching the MerchantProduct table.
     * Throws IllegalArgumentException if not found.
     */
    public Merchant getMerchantById(Long merchantId) {
        return merchantProductRepository.findAll().stream()
                .map(MerchantProduct::getMerchant)
                .filter(m -> m.getId().equals(merchantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));
    }

}