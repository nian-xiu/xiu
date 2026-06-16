package com.example.ssmshop.controller;

import com.example.ssmshop.dto.ProductFilter;
import com.example.ssmshop.service.CatalogService;
import com.example.ssmshop.service.CheckinService;
import com.example.ssmshop.service.FavoriteService;
import com.example.ssmshop.service.ProductBlacklistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
public class ShopController extends BaseController {
    private final CatalogService catalogService;
    private final CheckinService checkinService;
    private final FavoriteService favoriteService;
    private final ProductBlacklistService productBlacklistService;

    public ShopController(CatalogService catalogService, CheckinService checkinService, FavoriteService favoriteService,
                          ProductBlacklistService productBlacklistService) {
        this.catalogService = catalogService;
        this.checkinService = checkinService;
        this.favoriteService = favoriteService;
        this.productBlacklistService = productBlacklistService;
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        Long userId = currentUser(session) == null ? null : currentUserId(session);
        model.addAttribute("categories", catalogService.categories());
        model.addAttribute("featuredProducts", catalogService.featured(userId));
        model.addAttribute("latestProducts", catalogService.latest(8, userId));
        if (userId != null) {
            model.addAttribute("checkin", checkinService.status(userId));
        }
        return "shop/home";
    }

    @GetMapping("/products")
    public String products(ProductFilter filter, HttpSession session, Model model) {
        Long userId = currentUser(session) == null ? null : currentUserId(session);
        filter.setStatus("ON_SALE");
        model.addAttribute("categories", catalogService.categories());
        model.addAttribute("products", catalogService.search(filter, userId));
        model.addAttribute("filter", filter);
        return "shop/products";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        model.addAttribute("product", catalogService.findProduct(id));
        model.addAttribute("categories", catalogService.categories());
        if (currentUser(session) != null) {
            model.addAttribute("favorited", favoriteService.exists(currentUserId(session), id));
            model.addAttribute("blacklisted", productBlacklistService.exists(currentUserId(session), id));
        }
        return "shop/detail";
    }

    @GetMapping("/search")
    public String search(@RequestParam String keyword) {
        return "redirect:/products?keyword=" + UriUtils.encode(keyword, StandardCharsets.UTF_8);
    }
}
