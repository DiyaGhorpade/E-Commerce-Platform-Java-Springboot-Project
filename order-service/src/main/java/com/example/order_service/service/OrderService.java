package com.example.order_service.service;

import com.example.order_service.entity.Order;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);
    Order getOrderById(Long id);
    List<Order> getAllOrders();
    List<Order> getOrdersByUserId(Long userId);
    Order updateOrderStatus(Long id, Order.OrderStatus status);
    void deleteOrder(Long id);
}