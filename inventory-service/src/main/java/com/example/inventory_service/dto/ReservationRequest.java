package com.example.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationRequest {
    private Long orderId;
    private List<InventoryCheckRequest> items;
}