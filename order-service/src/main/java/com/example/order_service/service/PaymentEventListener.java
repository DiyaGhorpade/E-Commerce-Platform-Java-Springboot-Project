package com.example.order_service.service;

import com.example.order_service.dto.PaymentEvent;
import com.example.order_service.entity.Order.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        System.out.println("Ingested PaymentEvent from Kafka topology for Order ID: " + event.getOrderId() + " | Event Status: " + event.getStatus());

        if ("PAID".equals(event.getStatus())) {
            orderRepository.findById(event.getOrderId()).ifPresent(order -> {
                
                // Aligning external Kafka event with internal domain state
                order.setStatus(OrderStatus.CONFIRMED); 
                
                orderRepository.save(order);
                System.out.println("Distributed State Synchronized: Order " + event.getOrderId() + " transitioned to CONFIRMED in Order Service ledger.");
            });
        }
    }
}