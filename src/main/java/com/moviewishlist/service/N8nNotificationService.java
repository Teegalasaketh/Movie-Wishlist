package com.moviewishlist.service;

import com.moviewishlist.model.Movie;
import com.moviewishlist.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * N8N Notification Service
 * Integrates with n8n workflows to send email notifications
 * when movies are added or removed from wishlists.
 * 
 * This service communicates with n8n via webhook to trigger automated
 * email notifications without blocking the main application flow.
 */
@Service
public class N8nNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(N8nNotificationService.class);

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    @Value("${n8n.enabled:true}")
    private boolean n8nEnabled;

    private final RestTemplate restTemplate;

    public N8nNotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Notify when a movie is added to wishlist
     * Runs asynchronously to avoid blocking the add operation
     * 
     * @param movie The movie added to wishlist
     * @param user The user who added the movie
     */
    @Async
    public void notifyMovieAdded(Movie movie, User user) {
        if (!n8nEnabled) {
            logger.debug("N8n notifications disabled");
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventType", "MOVIE_ADDED");
            payload.put("movieId", movie.getId());
            payload.put("tmdbId", movie.getTmdbId());
            payload.put("movieTitle", movie.getTitle());
            payload.put("posterPath", movie.getPosterPath());
            payload.put("releaseYear", movie.getReleaseYear());
            payload.put("releaseDate", movie.getReleaseDate());
            payload.put("overview", movie.getOverview());
            payload.put("rating", movie.getRating());
            payload.put("userEmail", user.getEmail());
            payload.put("username", user.getUsername());
            payload.put("userId", user.getId());
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("action", "ADDED_TO_WISHLIST");

            sendWebhookRequest(payload);
            logger.info("✅ Movie added notification sent to n8n: {} by {}", movie.getTitle(), user.getUsername());

        } catch (Exception e) {
            logger.error("❌ Failed to send movie added notification to n8n", e);
            // Don't throw - notification failure shouldn't break the app
        }
    }

    /**
     * Notify when a movie is removed from wishlist
     * Runs asynchronously to avoid blocking the delete operation
     * 
     * @param movie The movie removed from wishlist
     * @param user The user who removed the movie
     */
    @Async
    public void notifyMovieRemoved(Movie movie, User user) {
        if (!n8nEnabled) {
            logger.debug("N8n notifications disabled");
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventType", "MOVIE_REMOVED");
            payload.put("movieId", movie.getId());
            payload.put("tmdbId", movie.getTmdbId());
            payload.put("movieTitle", movie.getTitle());
            payload.put("posterPath", movie.getPosterPath());
            payload.put("releaseYear", movie.getReleaseYear());
            payload.put("overview", movie.getOverview());
            payload.put("rating", movie.getRating());
            payload.put("userEmail", user.getEmail());
            payload.put("username", user.getUsername());
            payload.put("userId", user.getId());
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("action", "REMOVED_FROM_WISHLIST");

            sendWebhookRequest(payload);
            logger.info("✅ Movie removed notification sent to n8n: {} by {}", movie.getTitle(), user.getUsername());

        } catch (Exception e) {
            logger.error("❌ Failed to send movie removed notification to n8n", e);
        }
    }

    /**
     * Send generic webhook request to n8n
     * Handles HTTP communication with n8n server
     * 
     * @param payload The event data to send
     * @throws RestClientException if HTTP request fails
     */
    private void sendWebhookRequest(Map<String, Object> payload) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            String response = restTemplate.postForObject(n8nWebhookUrl, request, String.class);
            logger.debug("N8n webhook response: {}", response);
        } catch (RestClientException e) {
            logger.error("N8n webhook request failed to URL: {} - Error: {}", n8nWebhookUrl, e.getMessage());
            throw e;
        }
    }

    /**
     * Health check - verify n8n is reachable
     * Useful for monitoring and debugging
     * 
     * @return true if n8n webhook is accessible, false otherwise
     */
    public boolean isN8nHealthy() {
        return n8nEnabled;
    }
}