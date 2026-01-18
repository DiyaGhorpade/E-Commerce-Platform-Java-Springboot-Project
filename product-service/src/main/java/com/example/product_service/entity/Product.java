package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String description;
    
    // Using BigDecimal for money is best practice (float/double cause rounding errors)
    private BigDecimal price; 
    
    private int stockQuantity;

    private String imageUrl; // We only save the URL here, not the file!
}