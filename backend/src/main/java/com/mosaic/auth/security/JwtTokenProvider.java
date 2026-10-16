package com.mosaic.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Utility component for generating, signing, and parsing HMAC-SHA256 JWT tokens.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String TYPE_ACCESS = "ACCESS";
    public static final String TYPE_REFRESH = "REFRESH";

    @Value("${mosaic.jwt.secret:default-insecure-secret-key-that-must-be-replaced-in-production-env-32-bytes}")
    private String secret;

    @Value("${mosaic.jwt.access-token-expiry-ms:900000}")
    private long accessTokenExpiryMs; // 15 mins

    @Value("${mosaic.jwt.refresh-token-expiry-ms:604800000}")
    private long refreshTokenExpiryMs; // 7 days

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.warn("JWT secret key length is less than 256 bits (32 bytes). Padding for safety in dev/test.");
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TOKEN_TYPE, TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiryMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TOKEN_TYPE, TYPE_REFRESH)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public UUID extractUserId(String token) {
        Claims claims = getClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(String token) {
        Claims claims = getClaims(token);
        return claims.get(CLAIM_EMAIL, String.class);
    }

    public String extractTokenType(String token) {
        Claims claims = getClaims(token);
        return claims.get(CLAIM_TOKEN_TYPE, String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpiryMs / 1000;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
