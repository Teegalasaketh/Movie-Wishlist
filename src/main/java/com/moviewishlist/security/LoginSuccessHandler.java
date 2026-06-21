package com.moviewishlist.security;

import com.moviewishlist.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        HttpSession session = request.getSession();

        // Store logged-in user
        session.setAttribute("user", user);

        // Generate initials
        String username = user.getUsername() != null
                ? user.getUsername().trim()
                : "";

        String initials = "";

        if (!username.isEmpty()) {
            String[] parts = username.split("\\s+");

            if (parts.length >= 2) {
                initials = ""
                        + Character.toUpperCase(parts[0].charAt(0))
                        + Character.toUpperCase(parts[1].charAt(0));
            } else {
                initials = ""
                        + Character.toUpperCase(parts[0].charAt(0));
            }
        }

        session.setAttribute("userInitials", initials);

        // Redirect after successful login
        response.sendRedirect("/search");
    }
}