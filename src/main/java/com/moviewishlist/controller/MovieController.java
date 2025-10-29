package com.moviewishlist.controller;
import com.moviewishlist.model.Movie;
import com.moviewishlist.service.MovieService;
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

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // ✅ Default page = Search page
    @GetMapping("/")
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
    public ResponseEntity<String> addToWishlist(@RequestParam("tmdbId") long tmdbId) {
        try {
            movieService.addMovieToWishlist(tmdbId);
            return ResponseEntity.ok("success"); // ✅ proper HTTP 200 response
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("error"); // ✅ handle error safely
        }
    }

    // ✅ Wishlist page (for "View Wishlist" button)
    @GetMapping("/add-wishlist")
    public String viewWishlist(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "index"; // index.html = wishlist page
    }
    @PostMapping("/delete/{id}")
    public String deleteMovie(@PathVariable("id") long id) {
        movieService.deleteMovie(id);
        return "redirect:/add-wishlist"; // ✅ refresh wishlist after delete
    }

    @GetMapping("/add-custom")
    public String showAddCustomForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "add-movie"; // this will use add-movie.html
    }

    @PostMapping("/save")
    public String saveCustomMovie(@ModelAttribute Movie movie) {
        movieService.saveCustomMovie(movie);
        return "redirect:/add-wishlist"; // go back to wishlist after saving
    }
}
