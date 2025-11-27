package com.moviewishlist.controller;
import com.moviewishlist.model.Movie;
import com.moviewishlist.service.MovieService;
import com.moviewishlist.service.WishlistService;

import jakarta.servlet.http.HttpSession;
import com.moviewishlist.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
@Controller
public class MovieController {

    private final MovieService movieService;
    private final WishlistService wishlistService;
    public MovieController(MovieService movieService, WishlistService wishlistService) {
        this.movieService = movieService;
        this.wishlistService = wishlistService;
    }

    // ✅ Default page = Search page
    @GetMapping("/movies")
    public String home() {
        return "search";
    }

    // Handle search
    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query != null && !query.isEmpty()) {
            Map<String, Object> response = movieService.searchMovies(query);
            model.addAttribute("results", response.get("results"));
            model.addAttribute("query", query);
            if (response.isEmpty()) {
                model.addAttribute("errorMessage", "Unable to reach TMDb. Please try again later.");
            }
        }
        return "search";
    }

    // Add to wishlist
    @GetMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addToWishlist(@RequestParam("tmdbId") long tmdbId, HttpSession session) {
        try {
            Movie movie = movieService.addMovieToWishlist(tmdbId);

            User user = (User) session.getAttribute("user");

            wishlistService.addToWishlist(movie, user);

            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("error");
        }
    }


    // ✅ Wishlist page (for "View Wishlist" button)
    /* @GetMapping("/add-wishlist")
    public String viewWishlist(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "index"; // index.html = wishlist page
    } */
    @PostMapping("/delete/{id}")
    public String deleteMovie(@PathVariable("id") long id) {
        movieService.deleteMovie(id);
        return "redirect:/add-wishlist"; // ✅ refresh wishlist after delete
    }
    
    @GetMapping("/add-custom")
    public String showAddCustomForm(Model model) {

        model.addAttribute("movie", new Movie());

        // Add this if you want to show movies list on this page:
        model.addAttribute("movies", movieService.getAllMovies());

        return "add-movie";
    }


    @PostMapping("/save")
    public String saveCustomMovie(@ModelAttribute Movie movie) {
        movieService.saveCustomMovie(movie);
        return "redirect:/add-wishlist"; // go back to wishlist after saving
    }
}
