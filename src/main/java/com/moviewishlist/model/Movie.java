package com.moviewishlist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long tmdbId; // The numeric ID from The Movie Database

    private String title;
    private String posterPath;

    @Column(length = 1000)
    private String overview;

    private String releaseDate;

    // ✅ Add these new fields for manual movies
    private Integer releaseYear;
    private String director;
    private String cast;
    private Double rating;
    public String getFormattedReleaseDate() {

    if (releaseDate == null || releaseDate.isBlank()) {
        return "";
    }

    try {
        return java.time.LocalDate.parse(releaseDate)
                .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    } catch (Exception e) {
        return releaseDate;
    }
}
}
