package com.dem.Inventory.controller;

import com.dem.Inventory.dto.ItemLookupDto;
import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.model.Item;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.ItemRepository;
import com.dem.Inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/item/invoice")
public class InvoiceItemController {

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping
    public String showInvoiceForm(Model model) {
        model.addAttribute("invoiceItem", new InvoiceItem());
        model.addAttribute("invoiceItems", invoiceItemRepository.findAll());

        // Distinct Item Nos
        List<String> uniqueItemNos = itemRepository.findDistinctItemNos();
        model.addAttribute("itemNos", uniqueItemNos);

        model.addAttribute("items", itemRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());

        return "add_invoice_item";
    }

    @PostMapping("/save")
    public String saveInvoiceItem(@ModelAttribute InvoiceItem invoiceItem) {
        try {

            double amount = invoiceItem.getCostPrice() * invoiceItem.getQuantity();
            invoiceItem.setAmount(amount);

            invoiceItemRepository.save(invoiceItem);

            if (invoiceItem.getNo() != null) {
                // ✅ Existing record, show update toast
                return "redirect:/item/invoice?success=update";
            } else {
                // ✅ New record, show add toast
                return "redirect:/item/invoice?success=add";
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error saving invoice item: " + ex.getMessage());
        }
    }

    @GetMapping("/edit/{id}")
    public String editInvoiceItem(@PathVariable Long id, Model model) {
        InvoiceItem item = invoiceItemRepository.findById(id).orElseThrow();
        model.addAttribute("invoiceItem", item);
        model.addAttribute("invoiceItems", invoiceItemRepository.findAll());
        model.addAttribute("items", itemRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        return "add_invoice_item";
    }

    @GetMapping("/delete/{id}")
    public String deleteInvoiceItem(@PathVariable Long id) {
        invoiceItemRepository.deleteById(id);
        return "redirect:/item/invoice?success=delete";
    }

    @GetMapping("/{itemNo}")
    public ResponseEntity<ItemLookupDto> getByItemNo(@PathVariable String itemNo) {
        return itemRepository.findByItemNoIgnoreCase(itemNo)
                .map(it -> ResponseEntity.ok(new ItemLookupDto(it.getItemNo(), it.getItemName(), it.getItemType())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<ItemLookupDto> getByItemName(@PathVariable String name) {
        return itemRepository.findByItemNameIgnoreCase(name)
                .map(it -> ResponseEntity.ok(new ItemLookupDto(it.getItemNo(), it.getItemName(), it.getItemType())))
                .orElse(ResponseEntity.notFound().build());
    }

}

