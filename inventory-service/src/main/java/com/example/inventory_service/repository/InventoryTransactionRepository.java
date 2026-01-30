package com.example.inventory_service.repository;

import com.example.inventory_service.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByProductId(Long productId);

    List<InventoryTransaction> findByOrderId(Long orderId);

    List<InventoryTransaction> findByType(InventoryTransaction.TransactionType type);

    List<InventoryTransaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    List<InventoryTransaction> findByProductIdAndTransactionDateBetween(
            Long productId, LocalDateTime start, LocalDateTime end);
}