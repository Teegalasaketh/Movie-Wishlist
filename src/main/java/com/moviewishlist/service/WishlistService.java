package com.moviewishlist.service;

import com.moviewishlist.model.Movie;
import com.moviewishlist.model.User;
import com.moviewishlist.model.Wishlist;
import com.moviewishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public boolean addToWishlist(Movie movie, User user) {

        Optional<Wishlist> existing = wishlistRepository.findByUserAndMovie(user, movie);
        if (existing.isPresent()) {
            return false;
        }

        Wishlist w = new Wishlist();
        w.setMovie(movie);
        w.setUser(user);
        wishlistRepository.save(w);

        return true;
    }



    public List<Wishlist> getWishlist(User user) {
        return wishlistRepository.findByUser(user);
    }
    public void deleteById(Long id) {
        wishlistRepository.deleteById(id);
    }

}
