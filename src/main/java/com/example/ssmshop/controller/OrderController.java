package com.example.ssmshop.controller;

import com.example.ssmshop.domain.Order;
import com.example.ssmshop.dto.OrderDetail;
import com.example.ssmshop.service.AddressService;
import com.example.ssmshop.service.CartService;
import com.example.ssmshop.service.OrderService;
import com.example.ssmshop.service.ServiceMessageService;
import com.example.ssmshop.service.UserService;
import com.example.ssmshop.service.WechatPaySessionService;
import com.example.ssmshop.mapper.UserCouponMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController extends BaseController {
    private final CartService cartService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final ServiceMessageService serviceMessageService;
    private final UserService userService;
    private final WechatPaySessionService wechatPaySessionService;
    private final UserCouponMapper userCouponMapper;

    public OrderController(CartService cartService, AddressService addressService, OrderService orderService,
                           ServiceMessageService serviceMessageService, UserService userService,
                           WechatPaySessionService wechatPaySessionService, UserCouponMapper userCouponMapper) {
        this.cartService = cartService;
        this.addressService = addressService;
        this.orderService = orderService;
        this.serviceMessageService = serviceMessageService;
        this.userService = userService;
        this.wechatPaySessionService = wechatPaySessionService;
        this.userCouponMapper = userCouponMapper;
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        model.addAttribute("cart", cartService.summary(currentUserId(session)));
        model.addAttribute("checkoutMode", "CART");
        model.addAttribute("addresses", addressService.list(currentUserId(session)));
        model.addAttribute("profile", userService.findById(currentUserId(session)));
        model.addAttribute("coupons", userCouponMapper.findUnusedByUserId(currentUserId(session)));
        return "shop/checkout";
    }

    @GetMapping("/checkout/buy-now")
    public String buyNowCheckout(@RequestParam Long productId,
                                 @RequestParam(defaultValue = "1") int quantity,
                                 HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("cart", orderService.buyNowSummary(productId, quantity));
            model.addAttribute("checkoutMode", "BUY_NOW");
            model.addAttribute("buyNowProductId", productId);
            model.addAttribute("buyNowQuantity", quantity);
            model.addAttribute("addresses", addressService.list(currentUserId(session)));
            model.addAttribute("profile", userService.findById(currentUserId(session)));
            model.addAttribute("coupons", userCouponMapper.findUnusedByUserId(currentUserId(session)));
            return "shop/checkout";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam(required = false) Long addressId,
                             @RequestParam(defaultValue = "ONLINE") String paymentMethod,
                             @RequestParam(required = false) String wechatToken,
                             @RequestParam(required = false) String remark,
                             @RequestParam(required = false) String discountType,
                             @RequestParam(required = false) Long couponId,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            validateWechatConfirmation(paymentMethod, wechatToken, currentUserId(session));
            Order order = orderService.createFromCart(currentUserId(session), addressId, paymentMethod, remark, discountType, couponId);
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @PostMapping("/checkout/buy-now")
    public String placeBuyNowOrder(@RequestParam Long productId,
                                   @RequestParam(defaultValue = "1") int quantity,
                                   @RequestParam(required = false) Long addressId,
                                   @RequestParam(defaultValue = "ONLINE") String paymentMethod,
                                   @RequestParam(required = false) String wechatToken,
                                   @RequestParam(required = false) String remark,
                                   @RequestParam(required = false) String discountType,
                                   @RequestParam(required = false) Long couponId,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            validateWechatConfirmation(paymentMethod, wechatToken, currentUserId(session));
            Order order = orderService.createBuyNow(currentUserId(session), productId, quantity, addressId,
                    paymentMethod, remark, discountType, couponId);
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout/buy-now?productId=" + productId + "&quantity=" + Math.max(1, quantity);
        }
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        model.addAttribute("orders", orderService.userOrders(currentUserId(session)));
        return "user/orders";
    }

    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        OrderDetail detail = orderService.detailForUser(id, currentUserId(session));
        model.addAttribute("detail", detail);
        if (detail != null) {
            model.addAttribute("messages", serviceMessageService.findForUserOrder(id, currentUserId(session)));
            serviceMessageService.markUserRead(currentUserId(session), id);
            model.addAttribute("serviceUnreadCount", 0);
        }
        return "user/order-detail";
    }

    @PostMapping("/orders/{id}/refund")
    public String requestRefund(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderService.requestRefund(id, currentUserId(session));
            redirectAttributes.addFlashAttribute("message", "退款申请已提交，管理员会尽快处理");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/receive")
    public String confirmReceipt(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            orderService.confirmReceipt(id, currentUserId(session));
            redirectAttributes.addFlashAttribute("message", "已确认收货，感谢你的购买");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/contact")
    public String contactService(@PathVariable Long id, @RequestParam String message, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        OrderDetail detail = orderService.detailForUser(id, currentUserId(session));
        if (detail == null) {
            redirectAttributes.addFlashAttribute("error", "订单不存在");
            return "redirect:/orders";
        }
        try {
            serviceMessageService.createUserMessage(currentUserId(session), id, message);
            redirectAttributes.addFlashAttribute("message", "已为你联系在线客服，留言会同步给管理员");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    private void validateWechatConfirmation(String paymentMethod, String wechatToken, Long userId) {
        if ("WECHAT".equals(paymentMethod) && !wechatPaySessionService.consumeConfirmed(wechatToken, userId)) {
            throw new IllegalArgumentException("请先使用微信扫码并在手机端确认购买");
        }
    }
}
