package com.dem.Inventory.repository;

import com.dem.Inventory.model.Sale;
import com.dem.Inventory.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySale(Sale sale);
}

