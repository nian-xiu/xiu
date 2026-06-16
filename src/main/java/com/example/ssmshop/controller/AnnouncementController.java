package com.example.ssmshop.controller;

import com.example.ssmshop.service.AnnouncementService;
import com.example.ssmshop.service.RewardCenterService;
import com.example.ssmshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AnnouncementController extends BaseController {
    private final AnnouncementService announcementService;
    private final UserService userService;
    private final RewardCenterService rewardCenterService;

    public AnnouncementController(AnnouncementService announcementService, UserService userService,
                                   RewardCenterService rewardCenterService) {
        this.announcementService = announcementService;
        this.userService = userService;
        this.rewardCenterService = rewardCenterService;
    }

    @GetMapping("/announcements")
    public String announcements(HttpSession session, Model model) {
        Long userId = currentUser(session) == null ? null : currentUserId(session);
        model.addAttribute("announcements", announcementService.visibleAnnouncements());
        model.addAttribute("featuredAnnouncement", announcementService.featured());
        if (userId != null) {
            model.addAttribute("profile", userService.findById(userId));
            model.addAttribute("mailUnreadCount", rewardCenterService.unreadMailCount(userId));
        }
        return "user/announcements";
    }

    @GetMapping("/announcements/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = currentUser(session) == null ? null : currentUserId(session);
        model.addAttribute("announcement", announcementService.findVisibleById(id));
        if (userId != null) {
            model.addAttribute("profile", userService.findById(userId));
        }
        return "user/announcement-detail";
    }
}
