package com.example.inventory_service.service;

import com.example.inventory_service.dto.*;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.entity.InventoryTransaction;
import com.example.inventory_service.repository.InventoryRepository;
import com.example.inventory_service.repository.InventoryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Override
    @Transactional
    public Inventory createInventory(Inventory inventory) {
        if (inventoryRepository.existsByProductId(inventory.getProductId())) {
            throw new RuntimeException("Inventory already exists for product: " + inventory.getProductId());
        }
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found with id: " + id));
    }

    @Override
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
    }

    @Override
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public Inventory updateInventory(Long id, Inventory inventory) {
        Inventory existing = getInventoryById(id);

        if (inventory.getProductName() != null) {
            existing.setProductName(inventory.getProductName());
        }
        if (inventory.getQuantity() != null) {
            existing.setQuantity(inventory.getQuantity());
        }
        if (inventory.getReorderLevel() != null) {
            existing.setReorderLevel(inventory.getReorderLevel());
        }
        if (inventory.getReorderQuantity() != null) {
            existing.setReorderQuantity(inventory.getReorderQuantity());
        }
        if (inventory.getStatus() != null) {
            existing.setStatus(inventory.getStatus());
        }

        return inventoryRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = getInventoryById(id);
        inventoryRepository.delete(inventory);
    }

    @Override
    @Transactional
    public Inventory addStock(Long productId, Integer quantity, String notes) {
        Inventory inventory = getInventoryByProductId(productId);

        int quantityBefore = inventory.getQuantity();
        inventory.setQuantity(quantityBefore + quantity);
        inventory.setLastRestocked(LocalDateTime.now());

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(productId);
        transaction.setType(InventoryTransaction.TransactionType.RESTOCK);
        transaction.setQuantity(quantity);
        transaction.setQuantityBefore(quantityBefore);
        transaction.setQuantityAfter(inventory.getQuantity());
        transaction.setNotes(notes);
        transactionRepository.save(transaction);

        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory adjustStock(Long productId, Integer quantity, String notes) {
        Inventory inventory = getInventoryByProductId(productId);

        int quantityBefore = inventory.getQuantity();
        inventory.setQuantity(quantity);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(productId);
        transaction.setType(InventoryTransaction.TransactionType.ADJUSTMENT);
        transaction.setQuantity(quantity - quantityBefore);
        transaction.setQuantityBefore(quantityBefore);
        transaction.setQuantityAfter(quantity);
        transaction.setNotes(notes);
        transactionRepository.save(transaction);

        return inventoryRepository.save(inventory);
    }

    @Override
    public InventoryCheckResponse checkAvailability(Long productId, Integer quantity) {
        try {
            Inventory inventory = getInventoryByProductId(productId);
            int available = inventory.getAvailableQuantity();

            if (available >= quantity) {
                return new InventoryCheckResponse(
                        productId,
                        true,
                        available,
                        "Stock available"
                );
            } else {
                return new InventoryCheckResponse(
                        productId,
                        false,
                        available,
                        "Insufficient stock. Available: " + available + ", Required: " + quantity
                );
            }
        } catch (Exception e) {
            return new InventoryCheckResponse(
                    productId,
                    false,
                    0,
                    "Product not found in inventory"
            );
        }
    }

    @Override
    @Transactional
    public ReservationResponse reserveInventory(ReservationRequest request) {
        try {
            for (InventoryCheckRequest item : request.getItems()) {
                InventoryCheckResponse check = checkAvailability(item.getProductId(), item.getQuantity());
                if (!check.isAvailable()) {
                    return new ReservationResponse(
                            false,
                            "Cannot reserve: " + check.getMessage(),
                            request.getOrderId()
                    );
                }
            }

            for (InventoryCheckRequest item : request.getItems()) {
                Inventory inventory = getInventoryByProductId(item.getProductId());

                int reservedBefore = inventory.getReservedQuantity();
                inventory.setReservedQuantity(reservedBefore + item.getQuantity());

                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProductId(item.getProductId());
                transaction.setType(InventoryTransaction.TransactionType.RESERVE);
                transaction.setQuantity(item.getQuantity());
                transaction.setQuantityBefore(inventory.getQuantity());
                transaction.setQuantityAfter(inventory.getQuantity());
                transaction.setOrderId(request.getOrderId());
                transaction.setNotes("Reserved for order");
                transactionRepository.save(transaction);

                inventoryRepository.save(inventory);
            }

            return new ReservationResponse(
                    true,
                    "Inventory reserved successfully",
                    request.getOrderId()
            );

        } catch (Exception e) {
            return new ReservationResponse(
                    false,
                    "Reservation failed: " + e.getMessage(),
                    request.getOrderId()
            );
        }
    }

    @Override
    @Transactional
    public ReservationResponse releaseReservation(Long orderId) {
        try {
            List<InventoryTransaction> reservations = transactionRepository.findByOrderId(orderId)
                    .stream()
                    .filter(t -> t.getType() == InventoryTransaction.TransactionType.RESERVE)
                    .toList();

            if (reservations.isEmpty()) {
                return new ReservationResponse(
                        false,
                        "No reservations found for order",
                        orderId
                );
            }

            for (InventoryTransaction reservation : reservations) {
                Inventory inventory = getInventoryByProductId(reservation.getProductId());

                int reservedBefore = inventory.getReservedQuantity();
                inventory.setReservedQuantity(Math.max(0, reservedBefore - reservation.getQuantity()));

                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProductId(reservation.getProductId());
                transaction.setType(InventoryTransaction.TransactionType.RELEASE);
                transaction.setQuantity(reservation.getQuantity());
                transaction.setQuantityBefore(inventory.getQuantity());
                transaction.setQuantityAfter(inventory.getQuantity());
                transaction.setOrderId(orderId);
                transaction.setNotes("Released reservation - order cancelled");
                transactionRepository.save(transaction);

                inventoryRepository.save(inventory);
            }

            return new ReservationResponse(
                    true,
                    "Reservation released successfully",
                    orderId
            );

        } catch (Exception e) {
            return new ReservationResponse(
                    false,
                    "Release failed: " + e.getMessage(),
                    orderId
            );
        }
    }

    @Override
    @Transactional
    public ReservationResponse fulfillOrder(Long orderId) {
        try {
            List<InventoryTransaction> reservations = transactionRepository.findByOrderId(orderId)
                    .stream()
                    .filter(t -> t.getType() == InventoryTransaction.TransactionType.RESERVE)
                    .toList();

            if (reservations.isEmpty()) {
                return new ReservationResponse(
                        false,
                        "No reservations found for order",
                        orderId
                );
            }

            for (InventoryTransaction reservation : reservations) {
                Inventory inventory = getInventoryByProductId(reservation.getProductId());

                int quantityBefore = inventory.getQuantity();
                int reservedBefore = inventory.getReservedQuantity();

                inventory.setQuantity(quantityBefore - reservation.getQuantity());
                inventory.setReservedQuantity(Math.max(0, reservedBefore - reservation.getQuantity()));

                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProductId(reservation.getProductId());
                transaction.setType(InventoryTransaction.TransactionType.FULFILL);
                transaction.setQuantity(reservation.getQuantity());
                transaction.setQuantityBefore(quantityBefore);
                transaction.setQuantityAfter(inventory.getQuantity());
                transaction.setOrderId(orderId);
                transaction.setNotes("Order fulfilled");
                transactionRepository.save(transaction);

                inventoryRepository.save(inventory);
            }

            return new ReservationResponse(
                    true,
                    "Order fulfilled successfully",
                    orderId
            );

        } catch (Exception e) {
            return new ReservationResponse(
                    false,
                    "Fulfillment failed: " + e.getMessage(),
                    orderId
            );
        }
    }

    @Override
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    @Override
    public List<Inventory> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems();
    }

    @Override
    public List<Inventory> getInventoryByStatus(Inventory.InventoryStatus status) {
        return inventoryRepository.findByStatus(status);
    }

    @Override
    public List<InventoryTransaction> getTransactionsByProductId(Long productId) {
        return transactionRepository.findByProductId(productId);
    }

    @Override
    public List<InventoryTransaction> getTransactionsByOrderId(Long orderId) {
        return transactionRepository.findByOrderId(orderId);
    }
}