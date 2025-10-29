package com.moviewishlist.repository;

import com.moviewishlist.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    // Finds a movie by its TMDb ID to check for duplicates
    Optional<Movie> findByTmdbId(Long tmdbId);
    Optional<Movie> findByTitleIgnoreCase(String title); 
}