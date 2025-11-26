package com.moviewishlist.controller;

import com.moviewishlist.model.Movie;
import com.moviewishlist.model.User;
import com.moviewishlist.service.MovieService;
import com.moviewishlist.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WishlistController {

    private final WishlistService wishlistService;
    private final MovieService movieService;

    public WishlistController(WishlistService wishlistService, MovieService movieService) {
        this.wishlistService = wishlistService;
        this.movieService = movieService;
    }

    @PostMapping("/wishlist/add/{movieId}")
    public String addToWishlist(@PathVariable Long movieId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Movie movie = movieService.findById(movieId);

        wishlistService.addToWishlist(movie, user);
        return "redirect:/wishlist";
    }

    @GetMapping("/wishlist")
    public String viewWishlist(HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");

        model.addAttribute("movies", wishlistService.getWishlist(user));

        return "index";   // index.html shows wishlist
    }

}
