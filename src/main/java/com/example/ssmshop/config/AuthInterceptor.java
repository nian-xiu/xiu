package com.example.ssmshop.config;

import com.example.ssmshop.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            String redirect = resolveRedirectTarget(request);
            response.sendRedirect("/login?redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8));
            return false;
        }
        if (request.getRequestURI().startsWith("/admin") && !"ADMIN".equals(currentUser.getRole())) {
            response.sendRedirect("/");
            return false;
        }
        return true;
    }

    private String resolveRedirectTarget(HttpServletRequest request) {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String query = request.getQueryString();
            return query == null || query.isBlank() ? request.getRequestURI() : request.getRequestURI() + "?" + query;
        }
        return localRefererPath(request);
    }

    private String localRefererPath(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/";
        }
        try {
            URI refererUri = URI.create(referer);
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            boolean sameHost = serverName.equalsIgnoreCase(refererUri.getHost());
            boolean samePort = refererUri.getPort() == -1 || refererUri.getPort() == serverPort;
            if (!sameHost || !samePort) {
                return "/";
            }
            String path = refererUri.getRawPath();
            String query = refererUri.getRawQuery();
            if (path == null || path.isBlank()) {
                return "/";
            }
            return query == null || query.isBlank() ? path : path + "?" + query;
        } catch (IllegalArgumentException ex) {
            return "/";
        }
    }
}
