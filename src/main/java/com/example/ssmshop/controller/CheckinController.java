package com.example.ssmshop.controller;

import com.example.ssmshop.service.CheckinService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckinController extends BaseController {
    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping("/checkin")
    public String checkin(HttpSession session, RedirectAttributes redirectAttributes) {
        checkinService.checkin(currentUserId(session));
        redirectAttributes.addFlashAttribute("message", "签到成功，奖励已到账");
        return "redirect:/";
    }
}
