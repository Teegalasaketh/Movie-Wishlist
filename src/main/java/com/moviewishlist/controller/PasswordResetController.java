package com.moviewishlist.controller;

import com.moviewishlist.dto.ForgotPasswordRequest;
import com.moviewishlist.dto.ResetPasswordRequest;
import com.moviewishlist.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
            BindingResult result,
            HttpServletRequest httpRequest,
            Model model) {

        if (result.hasErrors()) {
            return "forgot-password";
        }

        String baseUrl = getBaseUrl(httpRequest);
        passwordResetService.initiatePasswordReset(request.getEmail(), baseUrl);

        // Always show success (prevents account enumeration)
        model.addAttribute("successMessage",
                "If an account with that email exists, a reset link has been sent. Please check your inbox.");
        return "forgot-password";
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        Optional<String> username = passwordResetService.validateToken(token);

        if (username.isEmpty()) {
            return "redirect:/reset-password-error";
        }

        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken(token);
        model.addAttribute("resetPasswordRequest", resetRequest);
        model.addAttribute("username", username.get());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("token", request.getToken());
            return "reset-password";
        }

        if (!request.passwordsMatch()) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
            model.addAttribute("token", request.getToken());
            return "reset-password";
        }

        boolean success = passwordResetService.resetPassword(request.getToken(), request.getPassword());

        if (!success) {
            return "redirect:/reset-password-error";
        }

        return "redirect:/reset-password-success";
    }

    // ── Result Pages ──────────────────────────────────────────────────────────

    @GetMapping("/reset-password-success")
    public String showSuccessPage() {
        return "reset-password-success";
    }

    @GetMapping("/reset-password-error")
    public String showErrorPage() {
        return "reset-password-error";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();

        if (("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)) {
            return scheme + "://" + serverName;
        }
        return scheme + "://" + serverName + ":" + port;
    }
}