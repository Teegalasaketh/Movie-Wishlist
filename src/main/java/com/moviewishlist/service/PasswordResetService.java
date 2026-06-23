package com.moviewishlist.service;

import com.moviewishlist.model.PasswordResetToken;
import com.moviewishlist.model.User;
import com.moviewishlist.repository.PasswordResetTokenRepository;
import com.moviewishlist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRY_MINUTES = 60; // 1 hour
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Initiates the forgot-password flow.
     * Always returns successfully to prevent account enumeration attacks.
     */
    @Transactional
    public void initiatePasswordReset(String email, String appBaseUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // Don't reveal whether the email exists
            log.info("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOpt.get();

        // Invalidate any existing tokens for this user
        tokenRepository.invalidateAllTokensForUser(user);

        // Generate a cryptographically secure token
        String rawToken = generateSecureToken();
        PasswordResetToken resetToken = new PasswordResetToken(rawToken, user, TOKEN_EXPIRY_MINUTES);
        tokenRepository.save(resetToken);

        String resetLink = appBaseUrl + "/reset-password?token=" + rawToken;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);

        log.info("Password reset email sent to user: {}", user.getUsername());
    }

    /**
     * Validates a reset token and returns the associated username (for display).
     * Returns empty if token is invalid or expired.
     */
    public Optional<String> validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(PasswordResetToken::isValid)
                .map(t -> t.getUser().getUsername());
    }

    /**
     * Completes the password reset — validates token and updates the password.
     * Returns true on success, false if the token is invalid/expired.
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token)
                .filter(PasswordResetToken::isValid);

        if (tokenOpt.isEmpty()) {
            log.warn("Password reset attempted with invalid/expired token.");
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used (one-time use)
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user: {}", user.getUsername());
        return true;
    }

    // ── Scheduled cleanup ────────────────────────────────────────────────────

    /**
     * Runs every hour to purge expired and used tokens from the database.
     */
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredAndUsedTokens(LocalDateTime.now());
        log.debug("Expired/used password reset tokens cleaned up.");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}