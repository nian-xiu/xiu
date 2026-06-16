package com.example.ssmshop.controller;

import com.example.ssmshop.domain.RewardMail;
import com.example.ssmshop.domain.UserCoupon;
import com.example.ssmshop.service.RewardCenterService;
import com.example.ssmshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RewardCenterController extends BaseController {
    private final RewardCenterService rewardCenterService;
    private final UserService userService;

    public RewardCenterController(RewardCenterService rewardCenterService, UserService userService) {
        this.rewardCenterService = rewardCenterService;
        this.userService = userService;
    }

    @GetMapping("/activity")
    public String activity(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        model.addAttribute("activities", rewardCenterService.userCampaigns(userId));
        model.addAttribute("profile", userService.findById(userId));
        return "user/activity";
    }

    @PostMapping("/activity/{id}/claim")
    public String claimActivity(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            rewardCenterService.claimCampaign(currentUserId(session), id);
            redirectAttributes.addFlashAttribute("message", "活动奖励已领取");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/activity";
    }

    @PostMapping("/redeem")
    public String redeemCode(@RequestParam String code, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            var redeemed = rewardCenterService.redeemCouponCode(currentUserId(session), code);
            redirectAttributes.addFlashAttribute("message", "兑换成功：" + redeemed.getRewardLabel());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/backpack";
    }

    @GetMapping("/backpack")
    public String backpack(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        var coupons = rewardCenterService.backpack(userId);
        model.addAttribute("profile", userService.findById(userId));
        model.addAttribute("coupons", coupons);
        model.addAttribute("activeCouponCount", coupons.stream().filter(UserCoupon::isActive).count());
        return "user/backpack";
    }

    @PostMapping("/backpack/coupons/{id}/delete")
    public String deleteCoupon(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            rewardCenterService.deleteInactiveCoupon(currentUserId(session), id);
            redirectAttributes.addFlashAttribute("message", "优惠券已清理");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/backpack";
    }

    @PostMapping("/backpack/coupons/clear-inactive")
    public String clearInactiveCoupons(HttpSession session, RedirectAttributes redirectAttributes) {
        int count = rewardCenterService.clearInactiveCoupons(currentUserId(session));
        if (count > 0) {
            redirectAttributes.addFlashAttribute("message", "已清理 " + count + " 张不可用优惠券");
        } else {
            redirectAttributes.addFlashAttribute("error", "没有可清理的优惠券");
        }
        return "redirect:/backpack";
    }

    @GetMapping("/mail")
    public String mail(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        rewardCenterService.markMailViewed(userId);
        var mails = rewardCenterService.mailbox(userId);
        model.addAttribute("mailUnreadCount", 0);
        model.addAttribute("profile", userService.findById(userId));
        model.addAttribute("mails", mails);
        model.addAttribute("mailUnreadLocalCount", mails.stream().filter(RewardMail::isUnread).count());
        model.addAttribute("claimableMailCount", mails.stream().filter(RewardMail::isClaimable).count());
        return "user/mail";
    }

    @PostMapping("/mail/{id}/claim")
    public String claimMail(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            rewardCenterService.claimMail(currentUserId(session), id);
            redirectAttributes.addFlashAttribute("message", "邮件奖励已领取");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/mail";
    }

    @PostMapping("/mail/claim-all")
    public String claimAllMails(HttpSession session, RedirectAttributes redirectAttributes) {
        int claimed = rewardCenterService.claimAllMails(currentUserId(session));
        if (claimed > 0) {
            redirectAttributes.addFlashAttribute("message", "已一键领取 " + claimed + " 封邮件奖励");
        } else {
            redirectAttributes.addFlashAttribute("error", "没有可以领取的邮件");
        }
        return "redirect:/mail";
    }
}
