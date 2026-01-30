package com.example.inventory_service.repository;

import com.example.inventory_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    List<Inventory> findByStatus(Inventory.InventoryStatus status);

    @Query("SELECT i FROM Inventory i WHERE i.quantity - i.reservedQuantity <= i.reorderLevel")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.quantity - i.reservedQuantity <= 0")
    List<Inventory> findOutOfStockItems();

    boolean existsByProductId(Long productId);
}