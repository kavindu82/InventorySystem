package com.dem.Inventory.controller;

import com.dem.Inventory.model.Item;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.ItemRepository;
import com.dem.Inventory.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;
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
    public String deleteItem(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Item item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            redirectAttributes.addFlashAttribute("error", "❌ Item not found.");
            return "redirect:/item";
        }

        boolean inUse = invoiceItemRepository.existsByItemNo(item.getItemNo());
        if (inUse) {
            redirectAttributes.addFlashAttribute("error", "❌ Cannot delete. Item is used in invoice items.");
            return "redirect:/item";
        }

        itemRepository.delete(item);
        redirectAttributes.addAttribute("success", "delete");
        return "redirect:/item/add?success=delete";
    }

    @GetMapping("/name-by-type")
    public String getItemNameByType(@RequestParam String itemType) {
        Optional<Item> item = itemRepository.findFirstByItemType(itemType);
        return item.map(Item::getItemName).orElse("");
    }

    @GetMapping("/check-invoice-usage/{itemNo}")
    @ResponseBody
    public Map<String, Boolean> checkInvoiceUsage(@PathVariable String itemNo) {
        boolean inUse = invoiceItemRepository.existsByItemNo(itemNo);
        return Collections.singletonMap("inUse", inUse);
    }

}
