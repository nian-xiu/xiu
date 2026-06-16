package com.example.ssmshop.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WechatPaySessionService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long EXPIRES_MINUTES = 10;

    private final Map<String, WechatPaySession> sessions = new ConcurrentHashMap<>();

    public WechatPaySession create(Long userId, String amountText) {
        cleanupExpired();
        String token = newToken();
        WechatPaySession session = new WechatPaySession(token, userId, amountText, LocalDateTime.now().plusMinutes(EXPIRES_MINUTES));
        sessions.put(token, session);
        return session;
    }

    public WechatPaySession find(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        WechatPaySession session = sessions.get(token);
        if (session == null || session.isExpired()) {
            sessions.remove(token);
            return null;
        }
        return session;
    }

    public boolean confirm(String token) {
        WechatPaySession session = find(token);
        if (session == null) {
            return false;
        }
        session.setConfirmed(true);
        return true;
    }

    public boolean consumeConfirmed(String token, Long userId) {
        WechatPaySession session = find(token);
        if (session == null || !session.isConfirmed() || !session.userId().equals(userId)) {
            return false;
        }
        sessions.remove(token);
        return true;
    }

    private String newToken() {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private void cleanupExpired() {
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public static final class WechatPaySession {
        private final String token;
        private final Long userId;
        private final String amountText;
        private final LocalDateTime expiresAt;
        private volatile boolean confirmed;

        private WechatPaySession(String token, Long userId, String amountText, LocalDateTime expiresAt) {
            this.token = token;
            this.userId = userId;
            this.amountText = amountText == null || amountText.isBlank() ? "待确认" : amountText;
            this.expiresAt = expiresAt;
        }

        public String token() {
            return token;
        }

        public Long userId() {
            return userId;
        }

        public String amountText() {
            return amountText;
        }

        public LocalDateTime expiresAt() {
            return expiresAt;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        private void setConfirmed(boolean confirmed) {
            this.confirmed = confirmed;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
}
