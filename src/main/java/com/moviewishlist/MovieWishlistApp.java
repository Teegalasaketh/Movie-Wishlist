package com.moviewishlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MovieWishlistApp {

    public static void main(String[] args) {
        SpringApplication.run(MovieWishlistApp.class, args);
    }
}