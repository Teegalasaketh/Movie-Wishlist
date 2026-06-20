package com.moviewishlist.controller;

import com.moviewishlist.model.Movie;
import com.moviewishlist.service.MovieService;
import com.moviewishlist.service.WishlistService;
import com.moviewishlist.service.N8nNotificationService;
import com.moviewishlist.model.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Movie Controller
 * Handles movie search, wishlist management, and custom movie additions
 * Integrated with n8n for automated notifications
 */
@Controller
public class MovieController {

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    private final MovieService movieService;
    private final WishlistService wishlistService;
    private final N8nNotificationService n8nNotificationService;

    public MovieController(MovieService movieService, 
                          WishlistService wishlistService,
                          N8nNotificationService n8nNotificationService) {
        this.movieService = movieService;
        this.wishlistService = wishlistService;
        this.n8nNotificationService = n8nNotificationService;
    }

    /**
     * Default page = Search page
     */
    @GetMapping("/movies")
    public String home(Model model) {

        model.addAttribute(
            "trending",
            movieService.getTrendingMovies().get("results")
        );

        model.addAttribute(
            "upcoming",
            movieService.getUpcomingMovies().get("results")
        );

        model.addAttribute(
            "actionMovies",
            movieService.getMoviesByGenre(28).get("results")
        );

        model.addAttribute(
            "comedyMovies",
            movieService.getMoviesByGenre(35).get("results")
        );

        model.addAttribute(
            "scifiMovies",
            movieService.getMoviesByGenre(878).get("results")
        );

        return "search";
    }

    /**
     * Search for movies via TMDB
     */
    @GetMapping("/search")
    public String search(
            @RequestParam(value = "query", required = false) String query,
            Model model) {

        model.addAttribute(
            "trending",
            movieService.getTrendingMovies().get("results")
        );

        model.addAttribute(
            "upcoming",
            movieService.getUpcomingMovies().get("results")
        );

        model.addAttribute(
            "actionMovies",
            movieService.getMoviesByGenre(28).get("results")
        );

        model.addAttribute(
            "comedyMovies",
            movieService.getMoviesByGenre(35).get("results")
        );

        model.addAttribute(
            "scifiMovies",
            movieService.getMoviesByGenre(878).get("results")
        );

        if (query != null && !query.isEmpty()) {
            Map<String, Object> response = movieService.searchMovies(query);

            model.addAttribute("results", response.get("results"));
            model.addAttribute("query", query);
        }

        return "search";
    }

    /**
     * Add movie to wishlist
     * 🎯 Triggers n8n notification via WishlistService
     */
    @GetMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addToWishlist(@RequestParam("tmdbId") long tmdbId, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            
            if (user == null) {
                logger.warn("❌ Cannot add to wishlist: user not found in session");
                return ResponseEntity.status(401).body("User not logged in");
            }

            // Fetch movie details from TMDB
            Movie movie = movieService.addMovieToWishlist(tmdbId);
            
            if (movie == null) {
                logger.error("❌ Could not fetch movie details from TMDB for ID: {}", tmdbId);
                return ResponseEntity.status(400).body("Could not fetch movie details");
            }

            // Add to wishlist (this triggers n8n notification automatically)
            boolean added = wishlistService.addToWishlist(movie, user);
            
            logger.info("🎬 Movie add result: {} - {}", added ? "added" : "exists", movie.getTitle());
            return ResponseEntity.ok(added ? "added" : "exists");

        } catch (Exception e) {
            logger.error("❌ Error adding movie to wishlist", e);
            return ResponseEntity.status(500).body("Error adding to wishlist: " + e.getMessage());
        }
    }

    /**
     * View user's wishlist
     */
    @GetMapping("/wishlist")
    public String viewWishlist(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            logger.warn("⚠️ User not found in session, redirecting to login");
            return "redirect:/login";
        }

        // Get user's wishlist
        var wishlistMovies = wishlistService.getUserWishlist(user);
        model.addAttribute("wishlist", wishlistMovies);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("wishlistCount", wishlistMovies.size());

        logger.info("📋 Loaded wishlist for user: {} ({} movies)", user.getUsername(), wishlistMovies.size());
        return "wishlist"; // wishlist.html template
    }

    /**
     * Delete movie from wishlist
     * 🎯 Triggers n8n notification via WishlistService
     */
    @PostMapping("/delete/{id}")
    public String deleteMovie(@PathVariable("id") long id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            
            if (user == null) {
                logger.warn("❌ Cannot delete: user not found in session");
                return "redirect:/login";
            }

            Movie movie = movieService.findById(id);
            
            if (movie == null) {
                logger.warn("⚠️ Movie not found with ID: {}", id);
                return "redirect:/wishlist";
            }

            // Remove from wishlist (this triggers n8n notification automatically)
            boolean removed = wishlistService.removeFromWishlist(movie, user);
            
            if (removed) {
                logger.info("🗑️ Movie removed from wishlist: {}", movie.getTitle());
            } else {
                logger.warn("⚠️ Movie was not in wishlist: {}", movie.getTitle());
            }

            return "redirect:/wishlist";

        } catch (Exception e) {
            logger.error("❌ Error deleting movie from wishlist", e);
            return "redirect:/wishlist";
        }
    }

    /**
     * Show custom movie addition form
     */
    @GetMapping("/add-custom")
    public String showAddCustomForm(Model model) {
        model.addAttribute("movie", new Movie());
        // Show user's current wishlist on this page (optional)
        model.addAttribute("movies", movieService.getAllMovies());
        return "add-movie";
    }

    /**
     * Save custom movie to wishlist
     * 🎯 Triggers n8n notification via WishlistService
     */
    @PostMapping("/save")
    public String saveCustomMovie(@ModelAttribute Movie movie, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            
            if (user == null) {
                logger.warn("❌ Cannot save: user not found in session");
                return "redirect:/login";
            }

            // Save movie normally
            Movie savedMovie = movieService.saveCustomMovie(movie);
            logger.info("💾 Custom movie saved: {}", savedMovie.getTitle());

            // Add movie to wishlist (this triggers n8n notification automatically)
            wishlistService.addToWishlist(savedMovie, user);
            logger.info("✅ Custom movie added to wishlist for user: {}", user.getUsername());

            return "redirect:/wishlist";

        } catch (Exception e) {
            logger.error("❌ Error saving custom movie", e);
            return "redirect:/add-custom";
        }
    }

    /**
     * Health check endpoint (useful for monitoring)
     */
    @GetMapping("/health")
    @ResponseBody
    public Map<String, Object> health() {
        boolean n8nHealthy = n8nNotificationService.isN8nHealthy();
        return Map.of(
            "status", "UP",
            "app", "Movie Wishlist",
            "n8n_connected", n8nHealthy,
            "timestamp", System.currentTimeMillis()
        );
    }
    @GetMapping("/autocomplete")
    @ResponseBody
    public Object autocomplete(@RequestParam String query) {

        if (query == null || query.trim().length() < 2) {
            return java.util.List.of();
        }

        Map<String, Object> response = movieService.searchMovies(query);

        return response.get("results");
    }
    @GetMapping("/live-search")
    @ResponseBody
    public Object liveSearch(@RequestParam String query) {

        if(query == null || query.trim().length() < 2){
            return java.util.List.of();
        }

        Map<String, Object> response =
                movieService.searchMovies(query);

        return response.get("results");
    }
}