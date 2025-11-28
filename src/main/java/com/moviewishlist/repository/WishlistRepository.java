package com.moviewishlist.repository;

import com.moviewishlist.model.Movie;
import com.moviewishlist.model.User;
import com.moviewishlist.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUser(User user);
    Optional<Wishlist> findByUserAndMovie(User user, Movie movie);

}
