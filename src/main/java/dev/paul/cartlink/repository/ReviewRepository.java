package dev.paul.cartlink.repository;

import dev.paul.cartlink.model.Review;
import dev.paul.cartlink.model.Customer;
import dev.paul.cartlink.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
