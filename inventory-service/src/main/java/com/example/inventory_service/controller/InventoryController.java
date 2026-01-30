package com.example.inventory_service.controller;

import com.example.inventory_service.dto.*;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.entity.InventoryTransaction;
import com.example.inventory_service.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        Inventory created = inventoryService.createInventory(inventory);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> inventories = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(
            @PathVariable Long id,
            @RequestBody Inventory inventory) {
        Inventory updated = inventoryService.updateInventory(id, inventory);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/product/{productId}/add-stock")
    public ResponseEntity<Inventory> addStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String notes) {
        Inventory inventory = inventoryService.addStock(productId, quantity, notes);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/product/{productId}/adjust-stock")
    public ResponseEntity<Inventory> adjustStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String notes) {
        Inventory inventory = inventoryService.adjustStock(productId, quantity, notes);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/check-availability")
    public ResponseEntity<InventoryCheckResponse> checkAvailability(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        InventoryCheckResponse response = inventoryService.checkAvailability(productId, quantity);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveInventory(
            @RequestBody ReservationRequest request) {
        ReservationResponse response = inventoryService.reserveInventory(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/release/{orderId}")
    public ResponseEntity<ReservationResponse> releaseReservation(@PathVariable Long orderId) {
        ReservationResponse response = inventoryService.releaseReservation(orderId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/fulfill/{orderId}")
    public ResponseEntity<ReservationResponse> fulfillOrder(@PathVariable Long orderId) {
        ReservationResponse response = inventoryService.fulfillOrder(orderId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        List<Inventory> items = inventoryService.getLowStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Inventory>> getOutOfStockItems() {
        List<Inventory> items = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Inventory>> getInventoryByStatus(
            @PathVariable Inventory.InventoryStatus status) {
        List<Inventory> items = inventoryService.getInventoryByStatus(status);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/transactions/product/{productId}")
    public ResponseEntity<List<InventoryTransaction>> getTransactionsByProductId(
            @PathVariable Long productId) {
        List<InventoryTransaction> transactions = inventoryService.getTransactionsByProductId(productId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/order/{orderId}")
    public ResponseEntity<List<InventoryTransaction>> getTransactionsByOrderId(
            @PathVariable Long orderId) {
        List<InventoryTransaction> transactions = inventoryService.getTransactionsByOrderId(orderId);
        return ResponseEntity.ok(transactions);
    }
}