package com.example.ssmshop.controller;

import com.example.ssmshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController extends BaseController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        model.addAttribute("cart", cartService.summary(currentUserId(session)));
        return "shop/cart";
    }

    @GetMapping("/cart/add")
    public String addFallback(@RequestParam(required = false) Long productId) {
        return productId == null ? "redirect:/products" : "redirect:/products/" + productId;
    }

    @PostMapping("/cart/add")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity,
                      HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            cartService.add(currentUserId(session), productId, quantity);
            redirectAttributes.addFlashAttribute("message", "已加入购物车");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @GetMapping("/cart/{id}/update")
    public String updateFallback() {
        return "redirect:/cart";
    }

    @PostMapping("/cart/{id}/update")
    public String update(@PathVariable Long id, @RequestParam int quantity, HttpSession session) {
        cartService.update(id, currentUserId(session), quantity);
        return "redirect:/cart";
    }

    @GetMapping("/cart/{id}/remove")
    public String removeFallback() {
        return "redirect:/cart";
    }

    @PostMapping("/cart/{id}/remove")
    public String remove(@PathVariable Long id, HttpSession session) {
        cartService.remove(id, currentUserId(session));
        return "redirect:/cart";
    }
}
