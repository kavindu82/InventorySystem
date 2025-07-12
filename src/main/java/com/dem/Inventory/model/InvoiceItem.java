package com.dem.Inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private int quantity;
    private String importSizeAndDimension;
    private double costPrice;
    private double sellingPrice;

    private String number;
    private LocalDate arrivalDate;
    private double amount;
}


