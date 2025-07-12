package com.dem.Inventory.repository;

import com.dem.Inventory.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    Optional<InvoiceItem> findByItemNo(String itemNo);


}
