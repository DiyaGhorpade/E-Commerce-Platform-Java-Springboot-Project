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
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Integer quantity;

    private Integer quantityBefore;

    private Integer quantityAfter;

    private Long orderId;

    private String reference;

    private String notes;

    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }

    public enum TransactionType {
        RESTOCK,
        RESERVE,
        RELEASE,
        FULFILL,
        ADJUSTMENT,
        DAMAGED,
        RETURN
    }
}