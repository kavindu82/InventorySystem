package com.dem.Inventory.controller;
import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.model.Item;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.ItemRepository;
import com.dem.Inventory.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemService itemService;


    @GetMapping("/add")
    public String viewItemPage(Model model) {
        model.addAttribute("item", new Item());
        model.addAttribute("itemList", itemService.getAllItems());
        return "add_item";
    }

    @PostMapping("/save")
    public String saveItem(@ModelAttribute("item") Item item, RedirectAttributes redirectAttributes) {
        boolean isNew = (item.getId() == null || item.getId().isEmpty());

        if (itemService.isDuplicate(item)) {
            redirectAttributes.addFlashAttribute("duplicate", true);
            return "redirect:/item/add";
        }

        itemService.saveItem(item);

        return "redirect:/item/add?success=" + (isNew ? "add" : "update");
    }



    @GetMapping("/edit/{id}")
    public String editItem(@PathVariable String id, Model model) {
        model.addAttribute("item", itemService.getItemById(id).get());
        model.addAttribute("itemList", itemService.getAllItems());
        return "add_item";
    }

    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable String id) {
        itemService.deleteItemById(id);
        return "redirect:/item/add?success=delete";
    }

    @GetMapping("/name-by-type")
    public String getItemNameByType(@RequestParam String itemType) {
        Optional<Item> item = itemRepository.findFirstByItemType(itemType);
        return item.map(Item::getItemName).orElse("");
    }

}
