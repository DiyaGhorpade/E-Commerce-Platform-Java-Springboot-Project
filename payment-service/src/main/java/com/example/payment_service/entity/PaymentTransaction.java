package com.example.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links back to Diya's Order Service. Must be unique to prevent double-charging an order.
    @Column(nullable = false, unique = true)
    private Long orderId; 

    // The ID returned by Stripe (e.g., pi_3MtwBwLkdIwHu7ix28a3tqPc)
    @Column(unique = true)
    private String stripePaymentIntentId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency; // e.g., "USD" or "INR"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        REQUIRES_PAYMENT_METHOD,
        REQUIRES_ACTION,
        PROCESSING,
        SUCCEEDED,
        CANCELED,
        FAILED
    }
}