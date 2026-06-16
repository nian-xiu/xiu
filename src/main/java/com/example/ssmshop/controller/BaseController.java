package com.example.ssmshop.controller;

import com.example.ssmshop.domain.User;
import jakarta.servlet.http.HttpSession;

abstract class BaseController {
    protected User currentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    protected Long currentUserId(HttpSession session) {
        User user = currentUser(session);
        if (user == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        return user.getId();
    }
}
