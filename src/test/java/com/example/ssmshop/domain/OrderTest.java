package com.example.ssmshop.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {
    @Test
    void statusHelpersShouldReflectRefundableAndReceivableStates() {
        Order order = new Order();
        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now().minusHours(2));

        assertTrue(order.isRefundable());
        assertTrue(order.isReceivable());
        assertTrue(order.getDeliveryText() != null && !order.getDeliveryText().isBlank());
    }

    @Test
    void paidOrderShouldExposeDeliveryCountdownText() {
        Order order = new Order();
        order.setStatus("PAID");
        order.setAutoShipAt(LocalDateTime.now().plusHours(6));

        assertTrue(order.getAutoShipHoursLeft() > 0);
        assertTrue(order.getDeliveryText() != null && !order.getDeliveryText().isBlank());
    }
}
