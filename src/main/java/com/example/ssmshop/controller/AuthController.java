package com.example.ssmshop.controller;

import com.example.ssmshop.domain.User;
import com.example.ssmshop.form.LoginForm;
import com.example.ssmshop.form.RegisterForm;
import com.example.ssmshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Pattern;

@Controller
public class AuthController {
    private static final Pattern POST_ONLY_REDIRECT = Pattern.compile(
            "^(/cart/add|/favorites/toggle|/blacklist/(add|remove)|/checkin|/service/messages|/activity/\\d+/claim|/mail/\\d+/claim|/mail/claim-all|/redeem|/admin/service(/.*)?|/admin/activities/(campaigns|mails|codes)(/.*)?|/admin/announcements/save|/admin/announcements/\\d+/(status|pinned)|/cart/\\d+/(update|remove)|/addresses/\\d+/delete|/admin/products/\\d+/delete|/admin/orders/\\d+/status|/admin/users/\\d+/status)(\\?.*)?$"
    );

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("redirect", safeRedirect(redirect));
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm loginForm, BindingResult bindingResult,
                        @RequestParam(required = false) String redirect, HttpSession session, Model model) {
        String target = safeRedirect(redirect);
        if (bindingResult.hasErrors()) {
            model.addAttribute("redirect", target);
            return "user/login";
        }
        User user = userService.login(loginForm.getUsername(), loginForm.getPassword());
        if (user == null) {
            model.addAttribute("error", "用户名或密码错误，或账号已停用");
            model.addAttribute("redirect", target);
            return "user/login";
        }
        session.setAttribute("currentUser", user);
        if (target != null) {
            return "redirect:" + target;
        }
        return "ADMIN".equals(user.getRole()) ? "redirect:/admin" : "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterForm registerForm, BindingResult bindingResult,
                           HttpSession session, Model model) {
        if (bindingResult.hasErrors()) {
            return "user/register";
        }
        try {
            User user = userService.register(registerForm);
            session.setAttribute("currentUser", user);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "user/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "已退出登录");
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logoutFallback(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "已退出登录");
        return "redirect:/";
    }

    private String safeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return null;
        }
        if (!redirect.startsWith("/") || redirect.startsWith("//") || redirect.contains("://")) {
            return null;
        }
        if ("/login".equals(redirect) || redirect.startsWith("/login?") || "/error".equals(redirect)
                || "/logout".equals(redirect) || POST_ONLY_REDIRECT.matcher(redirect).matches()) {
            return null;
        }
        return redirect;
    }
}
