package com.example.ssmshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class SecurityService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String salt;

    public SecurityService(@Value("${app.security.password-salt}") String salt) {
        this.salt = salt;
    }

    public String hashPassword(String username, String password) {
        return passwordEncoder.encode(password);
    }

    public boolean matches(String username, String rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedHash);
        }
        return legacySha256(username, rawPassword).equals(storedHash);
    }

    public boolean needsRehash(String storedHash) {
        return storedHash == null || !storedHash.startsWith("$2");
    }

    private String legacySha256(String username, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((username + ":" + password + ":" + salt).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
