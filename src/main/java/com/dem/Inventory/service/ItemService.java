package com.dem.Inventory.service;


import com.dem.Inventory.model.Item;
import com.dem.Inventory.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public void saveItem(Item item) {
        if (item.getId() == null || item.getId().isEmpty()) {
            item.setId(generateItemId());
        }
        itemRepository.save(item);
    }

    public Optional<Item> getItemById(String id) {
        return itemRepository.findById(id);
    }

    public void deleteItemById(String id) {
        itemRepository.deleteById(id);
    }

    public String generateItemId() {
        List<Item> items = itemRepository.findAll();
        int max = 0;

        for (Item i : items) {
            try {
                int num = Integer.parseInt(i.getId().replace("ID", ""));
                if (num > max) max = num;
            } catch (Exception ignored) {}
        }

        return String.format("ID%03d", max + 1);
    }

    public boolean isDuplicate(Item item) {
        return itemRepository.findAll().stream().anyMatch(existing ->
                !existing.getId().equals(item.getId()) &&
                        existing.getItemNo().equalsIgnoreCase(item.getItemNo()) &&
                        existing.getItemName().equalsIgnoreCase(item.getItemName()) &&
                        existing.getItemType().equalsIgnoreCase(item.getItemType())
        );
    }

}
