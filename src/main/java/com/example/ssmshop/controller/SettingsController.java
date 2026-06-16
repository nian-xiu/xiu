package com.example.ssmshop.controller;

import com.example.ssmshop.domain.User;
import com.example.ssmshop.form.PasswordForm;
import com.example.ssmshop.form.ProfileForm;
import com.example.ssmshop.service.AddressService;
import com.example.ssmshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SettingsController extends BaseController {
    private final UserService userService;
    private final AddressService addressService;

    public SettingsController(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }

    @GetMapping("/settings")
    public String settings(HttpSession session, Model model) {
        User user = userService.findById(currentUserId(session));
        model.addAttribute("profileForm", toProfileForm(user));
        model.addAttribute("passwordForm", new PasswordForm());
        model.addAttribute("addresses", addressService.list(user.getId()));
        return "user/settings";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@Valid @ModelAttribute ProfileForm profileForm, BindingResult bindingResult,
                                HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareSettingsModel(session, model, profileForm, new PasswordForm());
            return "user/settings";
        }
        try {
            User updated = userService.updateProfile(currentUserId(session), profileForm);
            session.setAttribute("currentUser", updated);
            redirectAttributes.addFlashAttribute("message", "个人信息已更新");
            return "redirect:/settings";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("profile.invalid", ex.getMessage());
            prepareSettingsModel(session, model, profileForm, new PasswordForm());
            return "user/settings";
        }
    }

    @PostMapping("/settings/password")
    public String updatePassword(@Valid @ModelAttribute PasswordForm passwordForm, BindingResult bindingResult,
                                 HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareSettingsModel(session, model, toProfileForm(userService.findById(currentUserId(session))), passwordForm);
            return "user/settings";
        }
        try {
            User updated = userService.changePassword(currentUserId(session), passwordForm);
            session.setAttribute("currentUser", updated);
            redirectAttributes.addFlashAttribute("message", "登录密码已更新");
            return "redirect:/settings";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("password.invalid", ex.getMessage());
            prepareSettingsModel(session, model, toProfileForm(userService.findById(currentUserId(session))), passwordForm);
            return "user/settings";
        }
    }

    private void prepareSettingsModel(HttpSession session, Model model, ProfileForm profileForm, PasswordForm passwordForm) {
        User user = userService.findById(currentUserId(session));
        model.addAttribute("profileForm", profileForm);
        model.addAttribute("passwordForm", passwordForm);
        model.addAttribute("addresses", addressService.list(user.getId()));
    }

    private ProfileForm toProfileForm(User user) {
        ProfileForm form = new ProfileForm();
        form.setUsername(user.getUsername());
        form.setNickname(user.getNickname());
        form.setPhone(user.getPhone());
        form.setEmail(user.getEmail());
        return form;
    }
}
