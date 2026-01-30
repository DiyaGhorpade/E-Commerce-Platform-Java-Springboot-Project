package com.example.inventory_service.service;

import com.example.inventory_service.dto.*;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.entity.InventoryTransaction;

import java.util.List;

public interface InventoryService {

    // Inventory CRUD
    Inventory createInventory(Inventory inventory);
    Inventory getInventoryById(Long id);
    Inventory getInventoryByProductId(Long productId);
    List<Inventory> getAllInventory();
    Inventory updateInventory(Long id, Inventory inventory);
    void deleteInventory(Long id);

    // Stock operations
    Inventory addStock(Long productId, Integer quantity, String notes);
    Inventory adjustStock(Long productId, Integer quantity, String notes);

    // Reservation operations
    InventoryCheckResponse checkAvailability(Long productId, Integer quantity);
    ReservationResponse reserveInventory(ReservationRequest request);
    ReservationResponse releaseReservation(Long orderId);
    ReservationResponse fulfillOrder(Long orderId);

    // Queries
    List<Inventory> getLowStockItems();
    List<Inventory> getOutOfStockItems();
    List<Inventory> getInventoryByStatus(Inventory.InventoryStatus status);

    // Transaction history
    List<InventoryTransaction> getTransactionsByProductId(Long productId);
    List<InventoryTransaction> getTransactionsByOrderId(Long orderId);
}