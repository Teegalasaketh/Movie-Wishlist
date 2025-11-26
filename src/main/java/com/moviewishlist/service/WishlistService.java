package com.moviewishlist.service;

import com.moviewishlist.model.Movie;
import com.moviewishlist.model.User;
import com.moviewishlist.model.Wishlist;
import com.moviewishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public void addToWishlist(Movie movie, User user) {
        Wishlist w = new Wishlist();
        w.setMovie(movie);
        w.setUser(user);
        wishlistRepository.save(w);
    }

    public List<Wishlist> getWishlist(User user) {
        return wishlistRepository.findByUser(user);
    }
}
