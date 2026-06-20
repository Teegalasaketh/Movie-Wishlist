package com.moviewishlist.controller;

import com.moviewishlist.model.User;
import com.moviewishlist.service.UserService;
import com.moviewishlist.security.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Principal principal,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam(required = false) String confirmPassword,
                                 HttpSession session) {
        if (principal == null) return "redirect:/login";
        if (confirmPassword != null && !confirmPassword.equals(newPassword)) {
            return "redirect:/?passwordMismatch";
        }

        String username = principal.getName();
        boolean ok = userService.changePassword(username, currentPassword, newPassword);
        if (ok) {
            // refresh session user
            User u = userService.getByUsername(username);
            session.setAttribute("user", u);
            return "redirect:/?passwordChanged";
        }
        return "redirect:/?passwordFailed";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Principal principal,
                                @RequestParam(required = false) String username,
                                @RequestParam(required = false) String email,
                                HttpSession session) {
        String current = principal.getName();
        try {
            User updated = userService.updateProfile(current, username, email);
            session.setAttribute("user", updated);
            // update initials if username changed
            String initials = updated.getUsername() != null ? updated.getUsername().trim() : "";
            if (!initials.isEmpty()) {
                String[] parts = initials.split("\\s+");
                String in = parts.length>1 ? (""+Character.toUpperCase(parts[0].charAt(0))+Character.toUpperCase(parts[1].charAt(0))) : (""+Character.toUpperCase(parts[0].charAt(0)));
                session.setAttribute("userInitials", in);
            }

            // Refresh the Spring Security authentication if the username changed
            if (!current.equals(updated.getUsername())) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null) {
                    Authentication newAuth = new UsernamePasswordAuthenticationToken(
                            new CustomUserDetails(updated),
                            authentication.getCredentials(),
                            authentication.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
            }

            return "redirect:/?profileUpdated";
        } catch (IllegalArgumentException ex) {
            return "redirect:/?profileUpdateFailed";
        }
    }
}
