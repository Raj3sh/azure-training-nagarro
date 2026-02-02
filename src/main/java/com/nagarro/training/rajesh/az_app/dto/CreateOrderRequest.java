package com.nagarro.training.rajesh.az_app.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Integer userId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
