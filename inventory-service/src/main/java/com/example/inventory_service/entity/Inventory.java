package com.example.inventory_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long productId;

    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    private Integer reservedQuantity = 0;

    private Integer reorderLevel;

    private Integer reorderQuantity;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    private LocalDateTime lastRestocked;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (status == null) {
            status = InventoryStatus.IN_STOCK;
        }
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
        updateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
        updateStatus();
    }

    private void updateStatus() {
        int availableQuantity = quantity - reservedQuantity;

        if (availableQuantity <= 0) {
            status = InventoryStatus.OUT_OF_STOCK;
        } else if (reorderLevel != null && availableQuantity <= reorderLevel) {
            status = InventoryStatus.LOW_STOCK;
        } else {
            status = InventoryStatus.IN_STOCK;
        }
    }

    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public enum InventoryStatus {
        IN_STOCK,
        LOW_STOCK,
        OUT_OF_STOCK,
        DISCONTINUED
    }
}