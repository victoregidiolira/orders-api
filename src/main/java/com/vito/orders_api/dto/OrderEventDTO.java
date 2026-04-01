package com.vito.orders_api.dto;

import com.vito.orders_api.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderEventDTO {
    private UUID orderId;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String eventType;
}