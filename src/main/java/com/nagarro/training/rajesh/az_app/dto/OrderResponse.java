package com.nagarro.training.rajesh.az_app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderResponse {
    private Integer id;
    private Integer userId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
