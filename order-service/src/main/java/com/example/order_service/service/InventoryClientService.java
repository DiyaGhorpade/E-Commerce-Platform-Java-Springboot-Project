package com.example.order_service.client;

import com.example.order_service.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${inventory.service.url:http://localhost:8084}")
    private String inventoryServiceUrl;

    public InventoryCheckResponse checkAvailability(Long productId, Integer quantity) {
        String url = String.format("%s/api/inventory/check-availability?productId=%d&quantity=%d",
                inventoryServiceUrl, productId, quantity);

        try {
            return restTemplate.getForObject(url, InventoryCheckResponse.class);
        } catch (Exception e) {
            // Return unavailable if service is down
            return new InventoryCheckResponse(productId, false, 0,
                    "Inventory service unavailable: " + e.getMessage());
        }
    }

    public ReservationResponse reserveInventory(ReservationRequest request) {
        String url = inventoryServiceUrl + "/api/inventory/reserve";

        try {
            return restTemplate.postForObject(url, request, ReservationResponse.class);
        } catch (Exception e) {
            return new ReservationResponse(false,
                    "Failed to reserve inventory: " + e.getMessage(), request.getOrderId());
        }
    }

    public ReservationResponse releaseReservation(Long orderId) {
        String url = String.format("%s/api/inventory/release/%d", inventoryServiceUrl, orderId);

        try {
            return restTemplate.postForObject(url, null, ReservationResponse.class);
        } catch (Exception e) {
            return new ReservationResponse(false,
                    "Failed to release reservation: " + e.getMessage(), orderId);
        }
    }

    public ReservationResponse fulfillOrder(Long orderId) {
        String url = String.format("%s/api/inventory/fulfill/%d", inventoryServiceUrl, orderId);

        try {
            return restTemplate.postForObject(url, null, ReservationResponse.class);
        } catch (Exception e) {
            return new ReservationResponse(false,
                    "Failed to fulfill order: " + e.getMessage(), orderId);
        }
    }
}