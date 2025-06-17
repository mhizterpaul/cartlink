package dev.paul.cartlink.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.model.Review;
import dev.paul.cartlink.merchant.model.Merchant;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCustomer(Customer customer);

    List<Review> findByMerchant(Merchant merchant);

    List<Review> findByRating(Integer rating);

    List<Review> findByMerchantAndRating(Merchant merchant, Integer rating);

    List<Review> findByCustomerAndRating(Customer customer, Integer rating);

    List<Review> findByMerchant_merchantId(Long merchantId);
}
