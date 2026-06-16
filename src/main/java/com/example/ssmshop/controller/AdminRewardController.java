package com.example.ssmshop.controller;

import com.example.ssmshop.form.CouponCodeForm;
import com.example.ssmshop.form.RewardCampaignForm;
import com.example.ssmshop.form.RewardMailForm;
import com.example.ssmshop.service.RewardCenterService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminRewardController extends BaseController {
    private final RewardCenterService rewardCenterService;

    public AdminRewardController(RewardCenterService rewardCenterService) {
        this.rewardCenterService = rewardCenterService;
    }

    @GetMapping("/admin/activities")
    public String activities(Model model) {
        ensureForms(model);
        model.addAttribute("campaigns", rewardCenterService.adminCampaigns());
        model.addAttribute("mails", rewardCenterService.adminMails());
        model.addAttribute("codes", rewardCenterService.adminCouponCodes());
        return "admin/rewards";
    }

    @PostMapping("/admin/activities/campaigns")
    public String createCampaign(@Valid RewardCampaignForm campaignForm, BindingResult bindingResult,
                                 HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return rewardsView(model, "campaignForm", campaignForm);
        }
        try {
            rewardCenterService.createCampaign(currentUser(session), campaignForm);
            redirectAttributes.addFlashAttribute("message", "活动已创建");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/activities";
    }

    @PostMapping("/admin/activities/campaigns/{id}/status")
    public String updateCampaignStatus(@PathVariable Long id, @RequestParam String status,
                                       RedirectAttributes redirectAttributes) {
        try {
            rewardCenterService.updateCampaignStatus(id, status);
            redirectAttributes.addFlashAttribute("message", "活动状态已更新");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/activities";
    }

    @PostMapping("/admin/activities/campaigns/{id}/clone")
    public String cloneCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rewardCenterService.cloneCampaign(id);
            redirectAttributes.addFlashAttribute("message", "活动已克隆为暂停状态，调整后启用即可");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/activities";
    }

    @PostMapping("/admin/activities/campaigns/{id}/delete")
    public String deleteCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        rewardCenterService.deleteCampaign(id);
        redirectAttributes.addFlashAttribute("message", "活动已删除");
        return "redirect:/admin/activities";
    }

    @PostMapping("/admin/activities/mails")
    public String createMail(@Valid RewardMailForm mailForm, BindingResult bindingResult,
                             HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return rewardsView(model, "mailForm", mailForm);
        }
        try {
            if (mailForm.isBroadcast()) {
                int count = rewardCenterService.broadcastMail(currentUser(session), mailForm);
                redirectAttributes.addFlashAttribute("message", "群发完成，共发送 " + count + " 封");
            } else {
                rewardCenterService.createMail(currentUser(session), mailForm);
                redirectAttributes.addFlashAttribute("message", "邮件已发送");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/activities";
    }

    @PostMapping("/admin/activities/codes")
    public String createCode(@Valid CouponCodeForm codeForm, BindingResult bindingResult,
                             HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return rewardsView(model, "codeForm", codeForm);
        }
        try {
            var code = rewardCenterService.createCouponCode(currentUser(session), codeForm);
            redirectAttributes.addFlashAttribute("message", "兑换码已生成：" + code.getCode());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/activities";
    }

    @PostMapping("/admin/activities/codes/{id}/status")
    public String updateCouponCodeStatus(@PathVariable Long id, @RequestParam String status,
                                         RedirectAttributes redirectAttributes) {
        try {
            rewardCenterService.updateCouponCodeStatus(id, status);
            redirectAttributes.addFlashAttribute("message", "兑换码状态已更新");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/activities";
    }

    @PostMapping("/admin/activities/codes/{id}/delete")
    public String deleteCouponCode(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        rewardCenterService.deleteCouponCode(id);
        redirectAttributes.addFlashAttribute("message", "兑换码已删除");
        return "redirect:/admin/activities";
    }

    private void ensureForms(Model model) {
        if (!model.containsAttribute("campaignForm")) {
            model.addAttribute("campaignForm", new RewardCampaignForm());
        }
        if (!model.containsAttribute("mailForm")) {
            model.addAttribute("mailForm", new RewardMailForm());
        }
        if (!model.containsAttribute("codeForm")) {
            model.addAttribute("codeForm", new CouponCodeForm());
        }
    }

    private String rewardsView(Model model, String keepKey, Object keepValue) {
        model.addAttribute(keepKey, keepValue);
        ensureForms(model);
        model.addAttribute("campaigns", rewardCenterService.adminCampaigns());
        model.addAttribute("mails", rewardCenterService.adminMails());
        model.addAttribute("codes", rewardCenterService.adminCouponCodes());
        return "admin/rewards";
    }
}
