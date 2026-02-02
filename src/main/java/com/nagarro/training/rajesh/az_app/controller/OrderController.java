package com.nagarro.training.rajesh.az_app.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.nagarro.training.rajesh.az_app.dto.CreateOrderRequest;
import com.nagarro.training.rajesh.az_app.dto.OrderResponse;
import com.nagarro.training.rajesh.az_app.dto.UpdateOrderStatusRequest;
import com.nagarro.training.rajesh.az_app.model.Order;
import com.nagarro.training.rajesh.az_app.repo.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        // Create new order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setProductName(request.getProductName());
        order.setQuantity(request.getQuantity());
        order.setPrice(request.getPrice());
        order.setTotalAmount(request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Convert to response DTO
        OrderResponse response = convertToOrderResponse(savedOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer id) {
        Optional<Order> order = orderRepository.findById(id);

        if (order.isPresent()) {
            OrderResponse response = convertToOrderResponse(order.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Integer id,
            @RequestBody UpdateOrderStatusRequest request) {
        Optional<Order> orderOptional = orderRepository.findById(id);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(request.getStatus());

            Order updatedOrder = orderRepository.save(order);
            OrderResponse response = convertToOrderResponse(updatedOrder);

            // Send notification via Azure Logic App if the order-status is 'COMPLETED'
            if ("COMPLETED".equalsIgnoreCase(response.getStatus())) {
                sendEmailNotificationOnOrderCompletion(order);
            }

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private void sendEmailNotificationOnOrderCompletion(Order order) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Spring RestTemplate");
            headers.set("Accept", "*/*");
            headers.set("Cache-Control", "no-cache");

            // Create request body with order details
            String requestBody = String.format(
                    "{\"orderId\": %d, \"status\": \"%s\"}",
                    order.getId(), order.getStatus());

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> laResponse = restTemplate.postForEntity(
                    "https://prod-26.centralindia.logic.azure.com:443/workflows/cc739adc9f1c4779a6eb9f18d575651c/triggers/Call_this_to_send_order_status_update_email/paths/invoke?api-version=2016-10-01&sp=/triggers/Call_this_to_send_order_status_update_email/run&sv=1.0&sig=HjNszGbD3H70MVMmfQTUgu9389RpTpV1O_-vjkXcy0c",
                    entity,
                    String.class);
            log.info("Notification sent successfully: {}", laResponse.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("Failed to send notification: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            log.error("Failed to send notification: " + e.getMessage() + "\\n\\n"
                    + ExceptionUtils.getRootCauseStackTrace(e));
        }
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setProductName(order.getProductName());
        response.setQuantity(order.getQuantity());
        response.setPrice(order.getPrice());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}