package com.dem.Inventory.repository;

import com.dem.Inventory.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    Optional<InvoiceItem> findByItemNo(String itemNo);

    @Query("SELECT COUNT(i) FROM InvoiceItem i WHERE i.quantity <= i.lowQuantity")
    int countLowStockItems();

    @Query("SELECT i FROM InvoiceItem i WHERE i.quantity <= i.lowQuantity ORDER BY i.quantity ASC")
    List<InvoiceItem> findLowStockItems();

    @Query("SELECT i FROM InvoiceItem i ")
    List<InvoiceItem> findAllStockItems();
}
