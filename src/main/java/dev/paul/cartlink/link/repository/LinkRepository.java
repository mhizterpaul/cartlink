package dev.paul.cartlink.link.repository;

import dev.paul.cartlink.link.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findBySlug(String slug);
}