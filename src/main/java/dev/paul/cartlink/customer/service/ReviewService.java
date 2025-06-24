package dev.paul.cartlink.customer.service;

import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.model.Review;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.customer.repository.ReviewRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository, CustomerRepository customerRepository,
            MerchantRepository merchantRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public List<Review> getReviewsByCustomer(Customer customer) {
        return reviewRepository.findByCustomer(customer);
    }

    public List<Review> getReviewsByMerchant(Merchant merchant) {
        return reviewRepository.findByMerchant(merchant);
    }

}
