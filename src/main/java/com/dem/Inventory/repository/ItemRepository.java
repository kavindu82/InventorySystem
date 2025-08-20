package com.dem.Inventory.repository;

import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item ,String> {

    @Query("SELECT DISTINCT i.itemNo FROM Item i")
    List<String> findDistinctItemNos();

    Optional<Item> findFirstByItemType(String itemType);

    Optional<Item> findByItemNoIgnoreCase(String itemNo);
    Optional<Item> findByItemNameIgnoreCase(String itemName);

}
