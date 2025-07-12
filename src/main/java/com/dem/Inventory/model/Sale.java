package com.dem.Inventory.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;
    private String clientName;
    private String clientContact;
    private LocalDate saleDate;
    private double discountPercentage;  // e.g., 5 for 5%
    private double discountAmount;      // Rs. value of discount
    private double totalAmount;         // Total before discount
    private double finalAmount;         // Total after discount
    private String paymentStatus; // PAID / PENDING


    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<SaleItem> items = new ArrayList<>();



}

