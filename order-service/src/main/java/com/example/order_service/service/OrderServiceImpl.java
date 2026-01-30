package com.example.order_service.service;

import com.example.order_service.client.InventoryClient;
import com.example.order_service.dto.*;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryClient inventoryClient;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // Set bidirectional relationship and calculate subtotals
        for (OrderItem item : order.getOrderItems()) {
            item.setOrder(order);

            if (item.getPrice() != null && item.getQuantity() > 0) {
                BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                item.setSubtotal(subtotal);
            } else {
                item.setSubtotal(BigDecimal.ZERO);
            }
        }

        // Calculate total amount
        BigDecimal total = order.getOrderItems().stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);

        // Check and reserve inventory
        List<InventoryCheckRequest> items = savedOrder.getOrderItems().stream()
                .map(item -> new InventoryCheckRequest(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());

        ReservationRequest reservationRequest = new ReservationRequest(savedOrder.getId(), items);
        ReservationResponse response = inventoryClient.reserveInventory(reservationRequest);

        if (!response.isSuccess()) {
            // Rollback order creation if inventory reservation fails
            orderRepository.delete(savedOrder);
            throw new RuntimeException("Failed to reserve inventory: " + response.getMessage());
        }

        return savedOrder;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = getOrderById(id);
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);

        // Handle inventory based on status changes
        if (status == Order.OrderStatus.SHIPPED && oldStatus != Order.OrderStatus.SHIPPED) {
            // Fulfill order - deduct from inventory
            ReservationResponse response = inventoryClient.fulfillOrder(id);
            if (!response.isSuccess()) {
                throw new RuntimeException("Failed to fulfill inventory: " + response.getMessage());
            }
        } else if (status == Order.OrderStatus.CANCELLED) {
            // Release reservation
            ReservationResponse response = inventoryClient.releaseReservation(id);
            if (!response.isSuccess()) {
                // Log warning but don't fail - order is already cancelled
                System.err.println("Warning: Failed to release inventory reservation: " +
                        response.getMessage());
            }
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);

        // Release inventory reservation if order is not completed
        if (order.getStatus() != Order.OrderStatus.DELIVERED &&
                order.getStatus() != Order.OrderStatus.CANCELLED) {
            inventoryClient.releaseReservation(id);
        }

        orderRepository.delete(order);
    }
}