package com.example.ssmshop.controller;

import com.example.ssmshop.domain.Announcement;
import com.example.ssmshop.form.AnnouncementForm;
import com.example.ssmshop.service.AnnouncementService;
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
public class AdminAnnouncementController extends BaseController {
    private final AnnouncementService announcementService;

    public AdminAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/admin/announcements")
    public String announcements(Model model) {
        if (!model.containsAttribute("announcementForm")) {
            model.addAttribute("announcementForm", new AnnouncementForm());
        }
        model.addAttribute("announcements", announcementService.allAnnouncements());
        return "admin/announcements";
    }

    @GetMapping("/admin/announcements/new")
    public String newPage(Model model) {
        model.addAttribute("announcementForm", new AnnouncementForm());
        return "admin/announcement-form";
    }

    @GetMapping("/admin/announcements/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        Announcement announcement = announcementService.findById(id);
        AnnouncementForm form = new AnnouncementForm();
        form.setId(announcement.getId());
        form.setTitle(announcement.getTitle());
        form.setSummary(announcement.getSummary());
        form.setContent(announcement.getContent());
        form.setCategory(announcement.getCategory());
        form.setStatus(announcement.getStatus());
        form.setPinned(announcement.pinnedEnabled());
        form.setRelatedCampaignId(announcement.getRelatedCampaignId());
        form.setPublishedAt(announcement.getPublishedAt());
        form.setExpiresAt(announcement.getExpiresAt());
        model.addAttribute("announcementForm", form);
        return "admin/announcement-form";
    }

    @PostMapping("/admin/announcements/save")
    public String save(@Valid AnnouncementForm form, BindingResult bindingResult, HttpSession session,
                       Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("announcementForm", form);
            return "admin/announcement-form";
        }
        try {
            Announcement announcement = new Announcement();
            announcement.setId(form.getId());
            announcement.setTitle(form.getTitle());
            announcement.setSummary(form.getSummary());
            announcement.setContent(form.getContent());
            announcement.setCategory(form.getCategory());
            announcement.setStatus(form.getStatus());
            announcement.setPinned(form.isPinned());
            announcement.setRelatedCampaignId(form.getRelatedCampaignId());
            announcement.setPublishedAt(form.getPublishedAt());
            announcement.setExpiresAt(form.getExpiresAt());
            announcementService.save(currentUser(session), announcement);
            redirectAttributes.addFlashAttribute("message", "公告已保存");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/announcements";
    }

    @PostMapping("/admin/announcements/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        try {
            announcementService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("message", "公告状态已更新");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/announcements";
    }

    @PostMapping("/admin/announcements/{id}/pinned")
    public String togglePinned(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            announcementService.togglePinned(id);
            redirectAttributes.addFlashAttribute("message", "公告置顶状态已切换");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/announcements";
    }
}
