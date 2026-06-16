package com.example.ssmshop.service;

import com.example.ssmshop.domain.Address;
import com.example.ssmshop.domain.Order;
import com.example.ssmshop.domain.OrderItem;
import com.example.ssmshop.domain.Product;
import com.example.ssmshop.domain.User;
import com.example.ssmshop.domain.UserCoupon;
import com.example.ssmshop.dto.CartLine;
import com.example.ssmshop.dto.CartSummary;
import com.example.ssmshop.dto.OrderDetail;
import com.example.ssmshop.mapper.CartMapper;
import com.example.ssmshop.mapper.OrderItemMapper;
import com.example.ssmshop.mapper.OrderMapper;
import com.example.ssmshop.mapper.ProductMapper;
import com.example.ssmshop.mapper.UserCouponMapper;
import com.example.ssmshop.mapper.UserMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CartMapper cartMapper;
    private final CartService cartService;
    private final AddressService addressService;
    private final UserMapper userMapper;
    private final UserCouponMapper userCouponMapper;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper, ProductMapper productMapper,
                        CartMapper cartMapper, CartService cartService, AddressService addressService,
                        UserMapper userMapper, UserCouponMapper userCouponMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.cartMapper = cartMapper;
        this.cartService = cartService;
        this.addressService = addressService;
        this.userMapper = userMapper;
        this.userCouponMapper = userCouponMapper;
    }

    @Transactional
    public Order createFromCart(Long userId, Long addressId, String paymentMethod, String remark) {
        return createFromCart(userId, addressId, paymentMethod, remark, null, null);
    }

    @Transactional
    public Order createFromCart(Long userId, Long addressId, String paymentMethod, String remark,
                                String discountType, Long couponId) {
        CartSummary summary = cartService.summary(userId);
        Order order = createFromSummary(userId, addressId, paymentMethod, remark, discountType, couponId, summary);
        cartMapper.clearByUserId(userId);
        return order;
    }

    public CartSummary buyNowSummary(Long productId, int quantity) {
        Product product = productMapper.findById(productId);
        if (product == null || !"ON_SALE".equals(product.getStatus())) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        int safeQuantity = Math.max(1, Math.min(quantity, product.getStock()));
        if (safeQuantity <= 0) {
            throw new IllegalArgumentException("商品库存不足");
        }
        CartLine line = new CartLine();
        line.setProduct(product);
        line.setQuantity(safeQuantity);
        line.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(safeQuantity)));
        CartSummary summary = new CartSummary();
        summary.getLines().add(line);
        summary.setTotalQuantity(safeQuantity);
        summary.setTotalAmount(line.getSubtotal());
        return summary;
    }

    @Transactional
    public Order createBuyNow(Long userId, Long productId, int quantity, Long addressId, String paymentMethod,
                              String remark, String discountType, Long couponId) {
        CartSummary summary = buyNowSummary(productId, quantity);
        return createFromSummary(userId, addressId, paymentMethod, remark, discountType, couponId, summary);
    }

    private Order createFromSummary(Long userId, Long addressId, String paymentMethod, String remark,
                                    String discountType, Long couponId, CartSummary summary) {
        if (summary.getLines().isEmpty()) {
            throw new IllegalArgumentException("购物车为空");
        }
        Address address = addressId == null ? addressService.defaultAddress(userId) : addressService.find(addressId, userId);
        if (address == null) {
            throw new IllegalArgumentException("请先添加收货地址");
        }

        BigDecimal originalAmount = summary.getTotalAmount();
        BigDecimal totalAmount = originalAmount;
        int coinUsed = 0;
        BigDecimal coinDiscount = BigDecimal.ZERO;
        Long usedCouponId = null;
        BigDecimal couponDiscount = BigDecimal.ZERO;

        if ("COINS".equals(discountType)) {
            User user = userMapper.findById(userId);
            int availableCoins = user.getCoins() == null ? 0 : user.getCoins();
            int maxUsableCoins = originalAmount.multiply(BigDecimal.valueOf(100)).intValue();
            coinUsed = Math.min((availableCoins / 100) * 100, maxUsableCoins);
            if (coinUsed > 0) {
                coinDiscount = BigDecimal.valueOf(coinUsed).divide(BigDecimal.valueOf(100));
                totalAmount = originalAmount.subtract(coinDiscount);
            }
        } else if ("COUPON".equals(discountType) && couponId != null) {
            UserCoupon coupon = userCouponMapper.findUnusedByIdAndUserId(couponId, userId);
            if (coupon == null) {
                throw new IllegalArgumentException("优惠券不可用");
            }
            usedCouponId = coupon.getId();
            if ("AMOUNT_OFF".equals(coupon.getCouponType())) {
                BigDecimal thresholdAmount = coupon.getThresholdAmount() == null ? BigDecimal.ZERO : coupon.getThresholdAmount();
                if (originalAmount.compareTo(thresholdAmount) < 0) {
                    throw new IllegalArgumentException("购物金额未满足满减门槛");
                }
                BigDecimal reduceAmount = coupon.getReduceAmount() == null ? BigDecimal.ZERO : coupon.getReduceAmount();
                couponDiscount = reduceAmount.min(originalAmount).setScale(2, RoundingMode.HALF_UP);
            } else {
                double discountRate = coupon.getDiscountRate() == null ? 0.90 : coupon.getDiscountRate();
                couponDiscount = originalAmount.multiply(BigDecimal.valueOf(1 - discountRate)).setScale(2, RoundingMode.HALF_UP);
            }
            totalAmount = originalAmount.subtract(couponDiscount);
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddressSnapshot(address.fullText());
        order.setOriginalAmount(originalAmount);
        order.setTotalAmount(totalAmount);
        order.setCoinUsed(coinUsed);
        order.setCoinDiscount(coinDiscount);
        order.setCouponId(usedCouponId);
        order.setCouponDiscount(couponDiscount);
        order.setEstimatedDeliveryDays(ThreadLocalRandom.current().nextInt(3, 8));
        order.setAutoShipAt(LocalDateTime.now().plusHours(24));
        order.setPaidAt(LocalDateTime.now());
        order.setStatus("PAID");
        order.setPaymentMethod(paymentMethod == null || paymentMethod.isBlank() ? "ONLINE" : paymentMethod);
        order.setRemark(remark);
        orderMapper.insert(order);

        for (CartLine line : summary.getLines()) {
            Product product = line.getProduct();
            int decreased = productMapper.decreaseStock(product.getId(), line.getQuantity());
            if (decreased == 0) {
                throw new IllegalArgumentException(product.getName() + " 库存不足");
            }
            productMapper.increaseSales(product.getId(), line.getQuantity());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setCoverUrl(product.getCoverUrl());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(line.getQuantity());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            orderItemMapper.insert(item);
        }
        if (coinUsed > 0 && userMapper.decreaseCoins(userId, coinUsed) == 0) {
            throw new IllegalArgumentException("金币余额不足");
        }
        if (usedCouponId != null) {
            int marked = userCouponMapper.markUsed(usedCouponId, userId, order.getId());
            if (marked == 0) {
                throw new IllegalArgumentException("优惠券已失效或已被使用，请重新选择");
            }
        }
        return order;
    }

    public List<Order> userOrders(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    public List<Order> allOrders(String status) {
        return orderMapper.findAll(status);
    }

    public OrderDetail detailForUser(Long orderId, Long userId) {
        Order order = orderMapper.findByIdAndUserId(orderId, userId);
        return buildDetail(order);
    }

    public OrderDetail detailForAdmin(Long orderId) {
        return buildDetail(orderMapper.findById(orderId));
    }

    public void updateStatus(Long id, String status) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!isValidTransition(order.getStatus(), status)) {
            throw new IllegalArgumentException("订单状态流转不合法");
        }
        orderMapper.updateStatus(id, status);
    }

    @Transactional
    public void requestRefund(Long orderId, Long userId) {
        Order order = orderMapper.findByIdAndUserId(orderId, userId);
        if (order == null || !order.isRefundable()) {
            throw new IllegalArgumentException("当前订单暂不能申请退款");
        }
        orderMapper.updateStatusByUserId(orderId, userId, "REFUND_REQUESTED");
    }

    @Transactional
    public void confirmReceipt(Long orderId, Long userId) {
        Order order = orderMapper.findByIdAndUserId(orderId, userId);
        if (order == null || !order.isReceivable()) {
            throw new IllegalArgumentException("订单尚未到达，暂不能确认收货");
        }
        orderMapper.updateStatusByUserId(orderId, userId, "COMPLETED");
    }

    private OrderDetail buildDetail(Order order) {
        if (order == null) {
            return null;
        }
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setItems(orderItemMapper.findByOrderId(order.getId()));
        return detail;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void autoShipPendingOrders() {
        try {
            for (Order order : orderMapper.findPendingAutoShip()) {
                if (order.getAutoShipAt() != null && LocalDateTime.now().isBefore(order.getAutoShipAt())) {
                    continue;
                }
                if (!"PAID".equals(order.getStatus()) || order.getShippedAt() != null) {
                    continue;
                }
                orderMapper.updateStatus(order.getId(), "SHIPPED");
            }
        } catch (DataAccessException ex) {
            return;
        }
    }

    private boolean isValidTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null || currentStatus.equals(targetStatus)) {
            return true;
        }
        return switch (currentStatus) {
            case "PAID" -> "SHIPPED".equals(targetStatus) || "REFUND_REQUESTED".equals(targetStatus) || "CANCELLED".equals(targetStatus);
            case "SHIPPED" -> "COMPLETED".equals(targetStatus) || "REFUND_REQUESTED".equals(targetStatus);
            case "REFUND_REQUESTED" -> "REFUNDED".equals(targetStatus) || "SHIPPED".equals(targetStatus);
            case "REFUNDED", "COMPLETED", "CANCELLED" -> false;
            default -> false;
        };
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "SSM" + timestamp + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
