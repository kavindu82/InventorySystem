package com.dem.Inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    private String itemNo;
    private String itemName;
    private String itemType;
    private String supplierName;
    @Column(nullable = false)
    private int quantity;
    private String importSizeAndDimension;
    private double costPrice;
    private double sellingPrice;
    @Column(nullable = false)
    private int lowQuantity;
    private LocalDate arrivalDate;
    private double amount;
    private String currency;  // e.g., USD, CNY, LKR
    private double originalCostPrice;  // in foreign currency

}


