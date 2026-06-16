package com.example.ssmshop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Set;

@Component
public class CsrfInterceptor implements HandlerInterceptor {
    public static final String SESSION_ATTRIBUTE = "csrfToken";
    private static final String PARAMETER_NAME = "_csrf";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        ensureToken(session);
        if (SAFE_METHODS.contains(request.getMethod().toUpperCase())) {
            return true;
        }
        String expectedToken = (String) session.getAttribute(SESSION_ATTRIBUTE);
        String actualToken = request.getParameter(PARAMETER_NAME);
        if (expectedToken != null && expectedToken.equals(actualToken)) {
            return true;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
        return false;
    }

    public static String ensureToken(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_ATTRIBUTE);
        if (token == null || token.isBlank()) {
            byte[] bytes = new byte[32];
            SECURE_RANDOM.nextBytes(bytes);
            token = HexFormat.of().formatHex(bytes);
            session.setAttribute(SESSION_ATTRIBUTE, token);
        }
        return token;
    }
}
