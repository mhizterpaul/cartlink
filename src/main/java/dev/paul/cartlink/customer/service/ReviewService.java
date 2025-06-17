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
    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;
    private final Random random = new Random();

    public ReviewService(ReviewRepository reviewRepository, CustomerRepository customerRepository,
            MerchantRepository merchantRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
        this.merchantRepository = merchantRepository;
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

    @Transactional
    public List<Review> createMockReviews() {
        List<Review> mockReviews = new ArrayList<>();
        List<Customer> customers = customerRepository.findAll();
        List<Merchant> merchants = merchantRepository.findAll();

        if (customers.isEmpty() || merchants.isEmpty()) {
            // Create mock customers and merchants if none exist
            if (customers.isEmpty()) {
                Customer customer1 = new Customer();
                customer1.setEmail("customer1@example.com");
                customer1.setFirstName("John");
                customer1.setLastName("Doe");
                customer1.setPhoneNumber("1234567890");
                customers.add(customerRepository.save(customer1));

                Customer customer2 = new Customer();
                customer2.setEmail("customer2@example.com");
                customer2.setFirstName("Jane");
                customer2.setLastName("Smith");
                customer2.setPhoneNumber("0987654321");
                customers.add(customerRepository.save(customer2));
            }

            if (merchants.isEmpty()) {
                Merchant merchant1 = new Merchant();
                merchant1.setEmail("merchant1@example.com");
                merchant1.setFirstName("Alice");
                merchant1.setLastName("Wonderland");
                merchant1.setPhoneNumber("1122334455");
                // Ensure wallet is created and set for merchant if your Merchant entity
                // requires it
                // merchant1.setWallet(new Wallet()); // Example, adjust as per your Wallet
                // entity
                merchants.add(merchantRepository.save(merchant1));

                Merchant merchant2 = new Merchant();
                merchant2.setEmail("merchant2@example.com");
                merchant2.setFirstName("Bob");
                merchant2.setLastName("The Builder");
                merchant2.setPhoneNumber("5544332211");
                // Ensure wallet is created and set for merchant if your Merchant entity
                // requires it
                // merchant2.setWallet(new Wallet()); // Example, adjust as per your Wallet
                // entity
                merchants.add(merchantRepository.save(merchant2));
            }
        }

        String[] comments = {
                "Great product, highly recommend!",
                "Good value for money.",
                "Not bad, but could be better.",
                "Excellent customer service.",
                "Fast shipping and well packaged.",
                "I'm very satisfied with my purchase.",
                "The quality is amazing.",
                "Exactly what I was looking for.",
                "Would buy again from this merchant.",
                "A bit disappointed with the product."
        };

        for (int i = 0; i < 10; i++) {
            Customer randomCustomer = customers.get(random.nextInt(customers.size()));
            Merchant randomMerchant = merchants.get(random.nextInt(merchants.size()));
            Review review = new Review();
            review.setCustomer(randomCustomer);
            review.setMerchant(randomMerchant);
            review.setRating(random.nextInt(5) + 1); // Rating between 1 and 5
            review.setComment(comments[random.nextInt(comments.length)]);
            review.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30))); // Random date in the last 30 days
            review.setUpdatedAt(LocalDateTime.now());
            mockReviews.add(reviewRepository.save(review));
        }
        return mockReviews;
    }
}
