package com.moviewishlist.service;

import com.moviewishlist.model.Movie;
import com.moviewishlist.model.User;
import com.moviewishlist.model.Wishlist;
import com.moviewishlist.repository.WishlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Wishlist Service
 * Manages user wishlists and integrates with n8n notifications
 * Handles adding/removing movies from user wishlists
 */
@Service
@Transactional
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistRepository wishlistRepository;
    private final N8nNotificationService n8nNotificationService;

    public WishlistService(WishlistRepository wishlistRepository, 
                          N8nNotificationService n8nNotificationService) {
        this.wishlistRepository = wishlistRepository;
        this.n8nNotificationService = n8nNotificationService;
    }

    /**
     * Add movie to user's wishlist
     * Triggers n8n notification if movie is successfully added
     * 
     * @param movie The movie to add
     * @param user The user adding the movie
     * @return true if movie was added, false if already existed
     */
    public boolean addToWishlist(Movie movie, User user) {
        if (movie == null || user == null) {
            logger.warn("Cannot add to wishlist: movie or user is null");
            return false;
        }

        // Check if movie already in wishlist
        Optional<Wishlist> existing = wishlistRepository.findByUserAndMovie(user, movie);
        
        if (existing.isPresent()) {
            logger.info("Movie {} already in wishlist for user {}", movie.getTitle(), user.getUsername());
            return false; // Movie already in wishlist
        }

        try {
            // Create new wishlist entry
            Wishlist wishlistEntry = new Wishlist();
            wishlistEntry.setUser(user);
            wishlistEntry.setMovie(movie);
            wishlistEntry.setAddedAt(System.currentTimeMillis());

            wishlistRepository.save(wishlistEntry);
            logger.info("✅ Movie {} added to wishlist for user {}", movie.getTitle(), user.getUsername());

            // 🎯 Trigger n8n notification AFTER successful save
            n8nNotificationService.notifyMovieAdded(movie, user);

            return true;

        } catch (Exception e) {
            logger.error("❌ Error adding movie to wishlist: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Remove movie from user's wishlist
     * Triggers n8n notification before removal
     * 
     * @param movie The movie to remove
     * @param user The user removing the movie
     * @return true if movie was removed, false if not found
     */
    public boolean removeFromWishlist(Movie movie, User user) {
        if (movie == null || user == null) {
            logger.warn("Cannot remove from wishlist: movie or user is null");
            return false;
        }

        try {
            Optional<Wishlist> wishlistEntry = wishlistRepository.findByUserAndMovie(user, movie);
            
            if (wishlistEntry.isEmpty()) {
                logger.info("Movie {} not found in wishlist for user {}", movie.getTitle(), user.getUsername());
                return false;
            }

            // 🎯 Trigger n8n notification BEFORE deletion
            n8nNotificationService.notifyMovieRemoved(movie, user);

            // Delete the entry
            wishlistRepository.delete(wishlistEntry.get());
            logger.info("✅ Movie {} removed from wishlist for user {}", movie.getTitle(), user.getUsername());

            return true;

        } catch (Exception e) {
            logger.error("❌ Error removing movie from wishlist: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get all movies in user's wishlist
     * 
     * @param user The user
     * @return List of movies in user's wishlist
     */
    public List<Movie> getUserWishlist(User user) {
        if (user == null) {
            return List.of();
        }
        return wishlistRepository.findMoviesByUser(user);
    }

    /**
     * Check if movie is in user's wishlist
     * 
     * @param movie The movie
     * @param user The user
     * @return true if movie is in user's wishlist
     */
    public boolean isInWishlist(Movie movie, User user) {
        if (movie == null || user == null) {
            return false;
        }
        return wishlistRepository.findByUserAndMovie(user, movie).isPresent();
    }

    /**
     * Get wishlist entry count for user
     * 
     * @param user The user
     * @return Number of movies in user's wishlist
     */
    public long getWishlistCount(User user) {
        if (user == null) {
            return 0;
        }
        return wishlistRepository.countByUser(user);
    }

    /**
     * Clear entire wishlist for user
     * 
     * @param user The user
     * @return Number of movies deleted
     */
    public long clearWishlist(User user) {
        if (user == null) {
            return 0;
        }
        long count = wishlistRepository.countByUser(user);
        wishlistRepository.deleteByUser(user);
        logger.info("✅ Wishlist cleared for user {}: {} movies removed", user.getUsername(), count);
        return count;
    }
}