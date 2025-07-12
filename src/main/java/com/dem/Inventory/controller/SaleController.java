package com.dem.Inventory.controller;

import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.model.Sale;
import com.dem.Inventory.model.SaleItem;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.SaleItemRepository;
import com.dem.Inventory.repository.SaleRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/sales")
public class SaleController {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @GetMapping("/add")
    public String showAddSalePage(Model model) {
        model.addAttribute("sale", new Sale());
        model.addAttribute("sales", saleRepository.findAll());
        return "add_sale";
    }

    @PostMapping("/save")
    public String saveSale(@ModelAttribute Sale sale, Model model) {
        String invoiceNumber;

        if (sale.getId() != null) {
            // Editing existing sale
            Sale existingSale = saleRepository.findById(sale.getId())
                    .orElseThrow(() -> new RuntimeException("Sale not found: " + sale.getId()));

            // Restore stock quantities for existing items
            for (SaleItem existingItem : existingSale.getItems()) {
                InvoiceItem stockItem = invoiceItemRepository.findByItemNo(existingItem.getItemNo())
                        .orElseThrow(() -> new RuntimeException("Item not found: " + existingItem.getItemNo()));
                stockItem.setQuantity(stockItem.getQuantity() + existingItem.getQuantity());
                invoiceItemRepository.save(stockItem);
            }

            invoiceNumber = existingSale.getInvoiceNumber();
            saleRepository.deleteById(sale.getId());
        } else {
            // New sale
            Optional<Sale> lastSale = saleRepository.findTopByOrderByIdDesc();
            Long nextId = lastSale.map(s -> s.getId() + 1).orElse(1L);
            invoiceNumber = String.format("S%03d", nextId);
        }

        sale.setInvoiceNumber(invoiceNumber);

        double totalAmount = 0.0;

        // Validate and deduct stock for new/updated items
        for (SaleItem item : sale.getItems()) {
            item.setSale(sale);
            InvoiceItem stockItem = invoiceItemRepository.findByItemNo(item.getItemNo())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + item.getItemNo()));

            if (stockItem.getQuantity() < item.getQuantity()) {
                model.addAttribute("errorMessage", "❌ Insufficient stock for item: " + item.getItemNo());
                model.addAttribute("sales", saleRepository.findAll());
                model.addAttribute("showModal", true);
                return "add_sale";
            }

            stockItem.setQuantity(stockItem.getQuantity() - item.getQuantity());
            invoiceItemRepository.save(stockItem);

            totalAmount += item.getAmount(); // Sum up item amounts
        }

        // ✅ Apply percentage discount
        double discountPercentage = sale.getDiscountPercentage(); // Add this field in Sale.java
        double discountAmount = (totalAmount * discountPercentage) / 100.0;
        double finalAmount = totalAmount - discountAmount;

        sale.setTotalAmount(totalAmount);          // Original total discountPercentage
        sale.setDiscountAmount(discountAmount);    // Discount amount
        sale.setFinalAmount(finalAmount);          // Total after discount

        saleRepository.save(sale);

        model.addAttribute("sales", saleRepository.findAll());
        model.addAttribute("sale", new Sale());
        model.addAttribute("successMessage", "✅ Sale saved successfully!");
        return "add_sale";
    }



    @GetMapping("/api/{id}")
    @ResponseBody
    public Sale getSaleDetails(@PathVariable Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sale ID: " + id));
    }

    @GetMapping("/items/{itemNo}")
    @ResponseBody
    public ResponseEntity<InvoiceItem> getItemDetails(@PathVariable String itemNo) {
        Optional<InvoiceItem> item = invoiceItemRepository.findByItemNo(itemNo);
        return item.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Inside @Controller class
    @GetMapping("/api/items")
    @ResponseBody
    public List<InvoiceItem> getAllItems() {
        return invoiceItemRepository.findAll();
    }

    @PostMapping("/update")
    @Transactional
    public String updateSale(@ModelAttribute Sale sale, Model model) {
        // Fetch the existing sale from DB
        Sale existingSale = saleRepository.findById(sale.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid sale ID: " + sale.getId()));

        // ✅ Restore stock for old sale items
        for (SaleItem oldItem : existingSale.getItems()) {
            InvoiceItem stockItem = invoiceItemRepository.findByItemNo(oldItem.getItemNo()).orElse(null);
            if (stockItem != null) {
                stockItem.setQuantity(stockItem.getQuantity() + oldItem.getQuantity());
                invoiceItemRepository.save(stockItem);
            }
        }

        // ✅ Update existing sale fields
        existingSale.setClientName(sale.getClientName());
        existingSale.setClientContact(sale.getClientContact());
        existingSale.setSaleDate(sale.getSaleDate());
        existingSale.setPaymentStatus(sale.getPaymentStatus());
        existingSale.setTotalAmount(sale.getTotalAmount());

        // ✅ Update or add sale items
        for (SaleItem updatedItem : sale.getItems()) {
            boolean found = false;

            for (SaleItem existingItem : existingSale.getItems()) {
                if (existingItem.getId() != null && existingItem.getId().equals(updatedItem.getId())) {
                    // Update existing SaleItem
                    existingItem.setItemNo(updatedItem.getItemNo());
                    existingItem.setItemName(updatedItem.getItemName());
                    existingItem.setQuantity(updatedItem.getQuantity());
                    existingItem.setSellingPrice(updatedItem.getSellingPrice());
                    existingItem.setAmount(updatedItem.getAmount());
                    found = true;
                    break;
                }
            }

            if (!found) {
                // New SaleItem
                updatedItem.setSale(existingSale);
                existingSale.getItems().add(updatedItem);
            }

            // ✅ Adjust stock for updated items
            InvoiceItem stockItem = invoiceItemRepository.findByItemNo(updatedItem.getItemNo())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + updatedItem.getItemNo()));
            if (stockItem.getQuantity() < updatedItem.getQuantity()) {
                model.addAttribute("errorMessage", "❌ Insufficient stock for item: " + updatedItem.getItemNo());
                model.addAttribute("sale", sale);
                model.addAttribute("sales", saleRepository.findAll());
                model.addAttribute("showModal", true);
                return "add_sale";
            }
            stockItem.setQuantity(stockItem.getQuantity() - updatedItem.getQuantity());
            invoiceItemRepository.save(stockItem);
        }

        // ✅ Save and flush changes
        entityManager.merge(existingSale);
        entityManager.flush();

        model.addAttribute("sales", saleRepository.findAll());
        model.addAttribute("sale", new Sale());
        model.addAttribute("successMessage", "✏️ Sale updated successfully!");
        return "add_sale";
    }

    @GetMapping("/delete/{id}")
    @Transactional
    public String deleteSale(@PathVariable Long id, Model model) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sale ID: " + id));

        // ✅ Restore stock for all sale items
        for (SaleItem item : sale.getItems()) {
            InvoiceItem stockItem = invoiceItemRepository.findByItemNo(item.getItemNo()).orElse(null);
            if (stockItem != null) {
                stockItem.setQuantity(stockItem.getQuantity() + item.getQuantity());
                invoiceItemRepository.save(stockItem);
            }
        }

        // ✅ Delete sale
        saleRepository.delete(sale);

        model.addAttribute("sales", saleRepository.findAll());
        model.addAttribute("sale", new Sale());
        model.addAttribute("successMessage", "🗑️ Sale deleted successfully!");
        return "add_sale";
    }

    @GetMapping("/invoice/{id}")
    public String getInvoiceHtml(@PathVariable Long id, Model model) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sale ID: " + id));
        model.addAttribute("sale", sale);
        return "invoice";
    }

}

