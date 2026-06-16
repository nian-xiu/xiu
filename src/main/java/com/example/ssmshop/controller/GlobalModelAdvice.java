package com.example.ssmshop.controller;

import com.example.ssmshop.config.CsrfInterceptor;
import com.example.ssmshop.domain.Announcement;
import com.example.ssmshop.domain.User;
import com.example.ssmshop.service.AnnouncementService;
import com.example.ssmshop.service.CartService;
import com.example.ssmshop.service.RewardCenterService;
import com.example.ssmshop.service.ServiceMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {
    private final CartService cartService;
    private final ServiceMessageService serviceMessageService;
    private final RewardCenterService rewardCenterService;
    private final AnnouncementService announcementService;

    public GlobalModelAdvice(CartService cartService, ServiceMessageService serviceMessageService,
                             RewardCenterService rewardCenterService, AnnouncementService announcementService) {
        this.cartService = cartService;
        this.serviceMessageService = serviceMessageService;
        this.rewardCenterService = rewardCenterService;
        this.announcementService = announcementService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return 0;
        }
        return cartService.summary(user.getId()).getTotalQuantity();
    }

    @ModelAttribute("serviceUnreadCount")
    public int serviceUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return 0;
        }
        if ("ADMIN".equals(user.getRole())) {
            return serviceMessageService.countAdminUnread();
        }
        return serviceMessageService.countUserUnread(user.getId());
    }

    @ModelAttribute("mailUnreadCount")
    public int mailUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || "ADMIN".equals(user.getRole())) {
            return 0;
        }
        return rewardCenterService.unreadMailCount(user.getId());
    }

    @ModelAttribute("featuredAnnouncement")
    public Announcement featuredAnnouncement() {
        return announcementService.featured();
    }

    @ModelAttribute("csrfToken")
    public String csrfToken(HttpSession session) {
        return CsrfInterceptor.ensureToken(session);
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
