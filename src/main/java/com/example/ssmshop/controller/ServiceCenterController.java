package com.example.ssmshop.controller;

import com.example.ssmshop.service.OrderService;
import com.example.ssmshop.service.ServiceMessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ServiceCenterController extends BaseController {
    private final ServiceMessageService serviceMessageService;
    private final OrderService orderService;

    public ServiceCenterController(ServiceMessageService serviceMessageService, OrderService orderService) {
        this.serviceMessageService = serviceMessageService;
        this.orderService = orderService;
    }

    @GetMapping("/service")
    public String userService(HttpSession session, Model model) {
        Long userId = currentUserId(session);
        serviceMessageService.markAllUserRead(userId);
        model.addAttribute("serviceUnreadCount", 0);
        model.addAttribute("messages", serviceMessageService.findForUser(userId));
        model.addAttribute("orders", orderService.userOrders(userId));
        return "user/service";
    }

    @PostMapping("/service/messages")
    public String sendUserMessage(@RequestParam(required = false) Long orderId,
                                  @RequestParam String message,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            serviceMessageService.createUserMessage(currentUserId(session), orderId, message);
            redirectAttributes.addFlashAttribute("message", "已发送给客服");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/service";
    }

    @GetMapping("/admin/service")
    public String adminService(Model model) {
        serviceMessageService.markAllAdminRead();
        model.addAttribute("serviceUnreadCount", 0);
        model.addAttribute("messages", serviceMessageService.findAll());
        return "admin/service";
    }

    @PostMapping("/admin/service/{orderId}/reply")
    public String replyOrderMessage(@PathVariable Long orderId,
                                    @RequestParam Long userId,
                                    @RequestParam String message,
                                    RedirectAttributes redirectAttributes) {
        try {
            serviceMessageService.createAdminReply(userId, orderId, message);
            serviceMessageService.markAdminRead(orderId);
            redirectAttributes.addFlashAttribute("message", "已回复用户");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    @PostMapping("/admin/service/reply")
    public String replyGeneralMessage(@RequestParam Long userId,
                                      @RequestParam(required = false) Long orderId,
                                      @RequestParam String message,
                                      RedirectAttributes redirectAttributes) {
        try {
            serviceMessageService.createAdminReply(userId, orderId, message);
            if (orderId != null) {
                serviceMessageService.markAdminRead(orderId);
            }
            redirectAttributes.addFlashAttribute("message", "已回复用户");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/service";
    }
}
