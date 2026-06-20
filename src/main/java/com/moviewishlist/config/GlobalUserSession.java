package com.moviewishlist.config;

import com.moviewishlist.model.User;
import com.moviewishlist.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalUserSession {

    private final UserService userService;

    public GlobalUserSession(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addUserToSession(HttpSession session, Principal principal) {
        if (principal == null) return; // user not logged in

        String username = principal.getName();
        if (username == null) return;

        Object sessionUser = session.getAttribute("user");
        if (sessionUser instanceof User existing && username.equals(existing.getUsername())) {
            return; // already loaded and current
        }

        userService.findByUsernameOptional(username).ifPresent(user -> {
            session.setAttribute("user", user);
            session.setAttribute("userInitials", buildInitials(user.getUsername()));
        });
    }

    private String buildInitials(String username) {
        if (username == null || username.isBlank()) return "?";

        String[] parts = username.split("\\s+");
        char first = Character.toUpperCase(parts[0].charAt(0));

        char second = (parts.length > 1)
                ? Character.toUpperCase(parts[1].charAt(0))
                : 0;

        return (second != 0) ? ("" + first + second) : ("" + first);
    }
}
