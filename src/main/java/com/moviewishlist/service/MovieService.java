package com.moviewishlist.service;

import com.moviewishlist.model.Movie;
import com.moviewishlist.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType; // <-- Added missing import
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder; // <-- Added for encoding query
import java.nio.charset.StandardCharsets; // <-- Added for encoding query
import java.util.Map;
import java.util.Optional;
@Service
public class MovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;

    // Use TMDB Read Access Token (v4)
    @Value("${tmdb.api.token}")
    private String apiToken;

    public MovieService(MovieRepository movieRepository, RestTemplate restTemplate) {
        this.movieRepository = movieRepository;
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> searchMovies(String query) {
        // Properly encode the query parameter to avoid issues with spaces/special chars
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.themoviedb.org/3/search/movie?query=" + encodedQuery;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("TMDb API error: {}", e.getMessage());
            return Map.of(); // ✅ returns an empty map, avoids NPE
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addMovieToWishlist(long tmdbId) {
        // ✅ Avoid duplicates by TMDb ID
        if (movieRepository.findByTmdbId(tmdbId).isPresent()) {
            logger.info("Movie with TMDb ID {} already exists in wishlist", tmdbId);
            return;
        }

        String url = "https://api.themoviedb.org/3/movie/" + tmdbId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> movieDetails = response.getBody();

            if (movieDetails != null) {
                Movie movie = new Movie();
                movie.setTmdbId(((Number) movieDetails.get("id")).longValue());
                movie.setTitle((String) movieDetails.get("title"));
                movie.setOverview((String) movieDetails.get("overview"));
                movie.setPosterPath((String) movieDetails.get("poster_path"));
                movie.setReleaseDate((String) movieDetails.get("release_date"));
                movieRepository.save(movie);
            }
        } catch (RestClientException e) {
            logger.error("Error fetching TMDb details for ID {}: {}", tmdbId, e.getMessage());
        }
    }

    // 🎥 Add custom movie manually
    public void saveCustomMovie(Movie movie) {
        // ✅ Avoid duplicates by title (case-insensitive)
        Optional<Movie> existing = movieRepository.findByTitleIgnoreCase(movie.getTitle());
        if (existing.isPresent()) {
            logger.info("Movie '{}' already exists in wishlist", movie.getTitle());
            return;
        }

        movieRepository.save(movie);
        logger.info("Custom movie '{}' added successfully", movie.getTitle());
    }
    public Iterable<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
    public void deleteMovie(long id) {
        movieRepository.deleteById(id);
    }
    @SuppressWarnings("null")
    public Movie findById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }

}