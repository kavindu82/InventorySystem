package com.dem.Inventory.controller;

import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.ItemRepository;
import com.dem.Inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ItemRepository itemRepository; // to load itemNo, name, etc.

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

}

