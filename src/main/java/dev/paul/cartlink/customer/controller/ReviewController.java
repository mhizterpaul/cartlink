package dev.paul.cartlink.customer.controller;

import dev.paul.cartlink.customer.model.Review;
import dev.paul.cartlink.customer.repository.ReviewRepository;
import dev.paul.cartlink.customer.service.ReviewService;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final MerchantRepository merchantRepository;

    public ReviewController(
            ReviewService reviewService,
            ReviewRepository reviewRepository,
            MerchantRepository merchantRepository) {
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
        this.merchantRepository = merchantRepository;
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<?> getReviewsByMerchant(@PathVariable Long merchantId) {
        Optional<Merchant> merchant = merchantRepository.findById(merchantId);
        if (merchant.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Merchant not found"));
        }

        List<Review> reviews = reviewRepository.findByMerchant(merchant.get());
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/merchant/email/{email}")
    public ResponseEntity<?> getReviewsByMerchantEmail(@PathVariable String email) {
        Optional<Merchant> merchant = merchantRepository.findByEmail(email);
        if (merchant.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Merchant not found"));
        }

        List<Review> reviews = reviewRepository.findByMerchant(merchant.get());
        return ResponseEntity.ok(reviews);
    }
}
