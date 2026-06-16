package com.example.ssmshop.controller;

import com.example.ssmshop.service.FavoriteService;
import com.example.ssmshop.service.ProductBlacklistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FavoriteController extends BaseController {
    private final FavoriteService favoriteService;
    private final ProductBlacklistService productBlacklistService;

    public FavoriteController(FavoriteService favoriteService, ProductBlacklistService productBlacklistService) {
        this.favoriteService = favoriteService;
        this.productBlacklistService = productBlacklistService;
    }

    @GetMapping("/favorites")
    public String favorites(HttpSession session, Model model) {
        model.addAttribute("products", favoriteService.list(currentUserId(session)));
        return "user/favorites";
    }

    @GetMapping("/blacklist")
    public String blacklist(HttpSession session, Model model) {
        model.addAttribute("products", productBlacklistService.list(currentUserId(session)));
        return "user/blacklist";
    }

    @GetMapping("/favorites/toggle")
    public String toggleFallback(@RequestParam(required = false) Long productId) {
        return productId == null ? "redirect:/products" : "redirect:/products/" + productId;
    }

    @PostMapping("/favorites/toggle")
    public String toggle(@RequestParam Long productId, HttpSession session) {
        favoriteService.toggle(currentUserId(session), productId);
        return "redirect:/products/" + productId;
    }

    @PostMapping("/blacklist/add")
    public String addBlacklist(@RequestParam Long productId, HttpSession session,
                               @RequestParam(required = false, defaultValue = "/products") String redirect) {
        productBlacklistService.add(currentUserId(session), productId);
        return "redirect:" + safeRedirect(redirect);
    }

    @PostMapping("/blacklist/remove")
    public String removeBlacklist(@RequestParam Long productId, HttpSession session,
                                  @RequestParam(required = false, defaultValue = "/blacklist") String redirect) {
        productBlacklistService.remove(currentUserId(session), productId);
        return "redirect:" + safeRedirect(redirect);
    }

    private String safeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank() || !redirect.startsWith("/") || redirect.startsWith("//") || redirect.contains("://")) {
            return "/products";
        }
        return redirect;
    }
}
