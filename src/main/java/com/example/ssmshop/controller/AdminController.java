package com.example.ssmshop.controller;

import com.example.ssmshop.domain.Product;
import com.example.ssmshop.dto.ProductFilter;
import com.example.ssmshop.form.ProductForm;
import com.example.ssmshop.service.AdminService;
import com.example.ssmshop.service.CatalogService;
import com.example.ssmshop.service.OrderService;
import com.example.ssmshop.service.ServiceMessageService;
import com.example.ssmshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final AdminService adminService;
    private final CatalogService catalogService;
    private final OrderService orderService;
    private final ServiceMessageService serviceMessageService;
    private final UserService userService;

    public AdminController(AdminService adminService, CatalogService catalogService, OrderService orderService,
                           ServiceMessageService serviceMessageService, UserService userService) {
        this.adminService = adminService;
        this.catalogService = catalogService;
        this.orderService = orderService;
        this.serviceMessageService = serviceMessageService;
        this.userService = userService;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.stats());
        model.addAttribute("latestProducts", catalogService.latest(5));
        model.addAttribute("orders", orderService.allOrders(null));
        return "admin/dashboard";
    }

    @GetMapping("/admin/products")
    public String products(ProductFilter filter, Model model) {
        model.addAttribute("products", catalogService.search(filter));
        model.addAttribute("categories", catalogService.allCategories());
        model.addAttribute("filter", filter);
        return "admin/products";
    }

    @GetMapping("/admin/products/new")
    public String createProductPage(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", catalogService.allCategories());
        model.addAttribute("action", "/admin/products");
        return "admin/product-form";
    }

    @PostMapping("/admin/products")
    public String createProduct(@Valid ProductForm productForm, BindingResult bindingResult,
                                @RequestParam(required = false) MultipartFile coverImage, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", catalogService.allCategories());
            model.addAttribute("action", "/admin/products");
            return "admin/product-form";
        }
        try {
            catalogService.saveProduct(productForm, coverImage);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.rejectValue("coverUrl", "coverImage.invalid", ex.getMessage());
            model.addAttribute("categories", catalogService.allCategories());
            model.addAttribute("action", "/admin/products");
            return "admin/product-form";
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{id}/edit")
    public String editProductPage(@PathVariable Long id, Model model) {
        Product product = catalogService.findProduct(id);
        model.addAttribute("productForm", catalogService.toForm(product));
        model.addAttribute("categories", catalogService.allCategories());
        model.addAttribute("action", "/admin/products/" + id);
        return "admin/product-form";
    }

    @GetMapping("/admin/products/{id}")
    public String editProductFallback(@PathVariable Long id) {
        return "redirect:/admin/products/" + id + "/edit";
    }

    @PostMapping("/admin/products/{id}")
    public String editProduct(@PathVariable Long id, @Valid ProductForm productForm, BindingResult bindingResult,
                              @RequestParam(required = false) MultipartFile coverImage, Model model) {
        productForm.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", catalogService.allCategories());
            model.addAttribute("action", "/admin/products/" + id);
            return "admin/product-form";
        }
        try {
            catalogService.saveProduct(productForm, coverImage);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.rejectValue("coverUrl", "coverImage.invalid", ex.getMessage());
            model.addAttribute("categories", catalogService.allCategories());
            model.addAttribute("action", "/admin/products/" + id);
            return "admin/product-form";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        catalogService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{id}/delete")
    public String deleteProductFallback() {
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/orders")
    public String orders(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("orders", orderService.allOrders(status));
        model.addAttribute("status", status);
        return "admin/orders";
    }

    @GetMapping("/admin/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("detail", orderService.detailForAdmin(id));
        model.addAttribute("messages", serviceMessageService.findForOrder(id));
        serviceMessageService.markAdminRead(id);
        return "admin/order-detail";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/admin/orders/{id}/status")
    public String updateOrderStatusFallback(@PathVariable Long id) {
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/admin/users/{id}/status")
    public String userStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            userService.updateStatus(id, status);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/{id}/status")
    public String userStatusFallback() {
        return "redirect:/admin/users";
    }
}
