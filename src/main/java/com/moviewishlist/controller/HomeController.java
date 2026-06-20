package com.moviewishlist.controller;

import com.moviewishlist.model.User;
import com.moviewishlist.security.CustomUserDetails;
import com.moviewishlist.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = null;

        if (username != null) {
            user = userService.findByUsernameOptional(username).orElse(null);
        }

        if (user == null) {
            Object sessionUser = session.getAttribute("user");
            if (sessionUser instanceof User) {
                user = (User) sessionUser;
            }
        }

        if (user == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
            }
        }

        if (user == null) {
            return "redirect:/login";
        }

        session.setAttribute("user", user);
        session.setAttribute("userInitials", buildInitials(user.getUsername()));

        return "redirect:/wishlist";
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
