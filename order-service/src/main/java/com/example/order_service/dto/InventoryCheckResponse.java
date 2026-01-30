package com.example.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryCheckResponse {
    private Long productId;
    private boolean available;
    private Integer availableQuantity;
    private String message;
}