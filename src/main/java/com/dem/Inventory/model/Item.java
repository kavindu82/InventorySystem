package com.dem.Inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    private String id;

    private String itemNo;
    private String itemName;
    private String itemType;
}

