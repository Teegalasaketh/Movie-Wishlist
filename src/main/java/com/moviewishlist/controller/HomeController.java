package com.moviewishlist.controller;

import com.moviewishlist.model.User;
import com.moviewishlist.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(HttpSession session, Principal principal) {

        // Principal contains the authenticated username from Spring Security
        String username = principal.getName();

        // Fetch full User from DB
        User user = userService.getByUsername(username);

        // Store user + initials in session (for navbar)
        session.setAttribute("user", user);
        session.setAttribute("userInitials", buildInitials(user.getUsername()));

        return "redirect:/wishlist"; // index.html template
    }

    private String buildInitials(String username) {
        if (username == null || username.isBlank()) return "?";

        String[] parts = username.trim().split("\\s+");
        char first = parts[0].charAt(0);

        char second = 0;
        if (parts.length > 1 && parts[1].length() > 0) {
            second = parts[1].charAt(0);
        }

        return second != 0
                ? ("" + Character.toUpperCase(first) + Character.toUpperCase(second))
                : ("" + Character.toUpperCase(first));
    }
}
