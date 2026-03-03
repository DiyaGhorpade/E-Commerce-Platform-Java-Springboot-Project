package com.example.payment_service.service;

import com.example.payment_service.dto.PaymentRequest;
import com.example.payment_service.dto.PaymentResponse;
import com.example.payment_service.entity.PaymentTransaction;
import com.example.payment_service.repository.PaymentTransactionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import com.example.payment_service.dto.PaymentEvent;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate; 

    @Value("${stripe.webhook.secret}") 
    private String endpointSecret;

    @Transactional
    public PaymentResponse createPaymentIntent(PaymentRequest request) throws StripeException {
        Optional<PaymentTransaction> existingTransaction = paymentRepository.findByOrderId(request.getOrderId());
        if (existingTransaction.isPresent()) {
            throw new IllegalStateException("A payment transaction already exists for Order ID: " + request.getOrderId());
        }

        long amountInSmallestUnit = request.getAmount().multiply(new BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInSmallestUnit)
                .setCurrency(request.getCurrency().toLowerCase())
                .putMetadata("orderId", request.getOrderId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .orderId(request.getOrderId())
                .stripePaymentIntentId(intent.getId())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .status(PaymentTransaction.PaymentStatus.REQUIRES_PAYMENT_METHOD)
                .build();
        
        paymentRepository.save(transaction);

        return PaymentResponse.builder()
                .clientSecret(intent.getClientSecret())
                .paymentIntentId(intent.getId())
                .status(intent.getStatus())
                .build();
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) throws StripeException {
        Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

        if ("payment_intent.succeeded".equals(event.getType()) || "payment_intent.payment_failed".equals(event.getType())) {
            String intentId = null;

            // Primary Heuristic: Strict Object Deserialization via Stripe SDK
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                StripeObject stripeObject = event.getDataObjectDeserializer().getObject().get();
                if (stripeObject instanceof PaymentIntent) {
                    intentId = ((PaymentIntent) stripeObject).getId();
                }
            } 
            
            // Fallback Heuristic: Raw JSON AST Traversal via Jackson (Bypasses SDK schema drift)
            if (intentId == null) {
                 try {
                     ObjectMapper mapper = new ObjectMapper();
                     JsonNode rootNode = mapper.readTree(payload);
                     intentId = rootNode.path("data").path("object").path("id").asText();
                     System.out.println("Fallback AST parsing utilized to extract Intent ID: " + intentId);
                 } catch (Exception e) {
                     System.err.println("Fatal extraction failure: Cannot traverse raw JSON AST payload.");
                     return;
                 }
            }

            if (intentId != null && !intentId.isEmpty()) {
                PaymentTransaction.PaymentStatus targetStatus = "payment_intent.succeeded".equals(event.getType()) 
                        ? PaymentTransaction.PaymentStatus.SUCCEEDED 
                        : PaymentTransaction.PaymentStatus.FAILED;
                
                updateStatus(intentId, targetStatus);
            }
        } else {
            System.out.println("Unhandled event taxonomy discarded: " + event.getType());
        }
    }

    private void updateStatus(String stripeId, PaymentTransaction.PaymentStatus status) {
        paymentRepository.findByStripePaymentIntentId(stripeId).ifPresent(tx -> {
            tx.setStatus(status);
            paymentRepository.save(tx);
            System.out.println("Payment " + stripeId + " synchronized to local ledger as: " + status);

            // IPC Broadcast: If payment is secured, notify the cluster
            if (status == PaymentTransaction.PaymentStatus.SUCCEEDED) {
                PaymentEvent event = PaymentEvent.builder()
                        .orderId(tx.getOrderId())
                        .status("PAID") // Translating internal status to external contract
                        .build();
                
                // We use the orderId as the Kafka Message Key to guarantee partition ordering
                kafkaTemplate.send("payment-events", String.valueOf(tx.getOrderId()), event);
                System.out.println("Published PaymentEvent to Kafka topology for Order ID: " + tx.getOrderId());
            }
        });
    }
}