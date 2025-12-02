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
import java.util.Map;
import java.util.Optional;
import java.net.URLEncoder; // <-- Added for encoding query
import java.nio.charset.StandardCharsets; // <-- Added for encoding query
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Movie addMovieToWishlist(long tmdbId) {

    // 1) If movie already exists in DB → return it
        Optional<Movie> existing = movieRepository.findByTmdbId(tmdbId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2) Fetch TMDB and create a new Movie
        String url = "https://api.themoviedb.org/3/movie/" + tmdbId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> movieDetails = response.getBody();

            if (movieDetails != null) {

                Movie movie = new Movie();
                movie.setTmdbId(((Number) movieDetails.get("id")).longValue());
                movie.setTitle((String) movieDetails.get("title"));
                movie.setOverview((String) movieDetails.get("overview"));
                movie.setPosterPath((String) movieDetails.get("poster_path"));

                // FULL release date (ex: 2010-07-16)
                movie.setReleaseDate((String) movieDetails.get("release_date"));

                // Extract year (if exists)
                String releaseDate = (String) movieDetails.get("release_date");
                if (releaseDate != null && releaseDate.length() >= 4) {
                    movie.setReleaseYear(Integer.parseInt(releaseDate.substring(0, 4)));
                }

                // TMDB rating
                Object vote = movieDetails.get("vote_average");
                if (vote != null) {
                    movie.setRating(((Number) vote).doubleValue());
                }

                return movieRepository.save(movie);
            }

        } catch (RestClientException e) {
            logger.error("Error fetching TMDb details for ID {}: {}", tmdbId, e.getMessage());
        }

        return null;
    }



    // 🎥 Add custom movie manually
    public Movie saveCustomMovie(Movie movie) {

        Optional<Movie> existing = movieRepository.findByTitleIgnoreCase(movie.getTitle());
        if (existing.isPresent()) {
            return existing.get();   // return existing movie
        }

        // Default fields for custom movies
        movie.setTmdbId(null);
        movie.setPosterPath(null);
        movie.setOverview("Custom added movie");
        movie.setReleaseDate(movie.getReleaseYear() + "-01-01");
        movie.setCast("Unknown");

        return movieRepository.save(movie);   // return saved movie
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