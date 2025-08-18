package com.dem.Inventory.controller;

import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.model.Sale;
import com.dem.Inventory.model.SaleItem;
import com.dem.Inventory.model.SaleType;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.SaleItemRepository;
import com.dem.Inventory.repository.SaleRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.naming.Context;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sales")
public class SaleController {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @GetMapping("/add")
    public String showAddSalePage(Model model) {
        model.addAttribute("sale", new Sale());
        model.addAttribute("sales", saleRepository.findByType(SaleType.SALE));
        return "add_sale";
    }

    @PostMapping("/save")
    @Transactional
    public String saveSale(@ModelAttribute Sale sale, Model model) {
        Sale existingSale = null;

        if (sale.getId() != null) {
            // Fetch existing sale for update
            existingSale = saleRepository.findById(sale.getId())
                    .orElseThrow(() -> new RuntimeException("Sale not found: " + sale.getId()));
        }

        String invoiceNumber;
        if (existingSale == null) {
            // New Sale: Generate invoice number
            Optional<Sale> lastSale = saleRepository.findTopByOrderByIdDesc();
            Long nextId = lastSale.map(s -> s.getId() + 1).orElse(1L);
            invoiceNumber = String.format("S%03d", nextId);
            sale.setInvoiceNumber(invoiceNumber);
            sale.setType(SaleType.SALE);
        } else {
            // Existing Sale: Use same invoice number
            invoiceNumber = existingSale.getInvoiceNumber();
            sale.setInvoiceNumber(invoiceNumber);
            sale.setType(SaleType.SALE);
        }

        // Map existing items for quick lookup
        Map<Long, SaleItem> existingItemsMap = new HashMap<>();
        if (existingSale != null) {
            for (SaleItem existingItem : existingSale.getItems()) {
                existingItemsMap.put(existingItem.getId(), existingItem);
            }
        }

        double totalAmount = 0.0;
        List<SaleItem> updatedItems = new ArrayList<>();

        for (SaleItem updatedItem : sale.getItems()) {
            updatedItem.setSale(sale);

            // ✅ Fetch stock item
            InvoiceItem stockItem = invoiceItemRepository.findByItemNo(updatedItem.getItemNo())
                    .orElseThrow(() -> new RuntimeException("Item not found in inventory: " + updatedItem.getItemNo()));

            if (updatedItem.getId() != null && existingItemsMap.containsKey(updatedItem.getId())) {
                // Update existing SaleItem
                SaleItem existingItem = existingItemsMap.get(updatedItem.getId());

                // ✅ Restore stock for old quantity
                stockItem.setQuantity(stockItem.getQuantity() + existingItem.getQuantity());

                // ✅ Deduct stock for new quantity
                if (stockItem.getQuantity() < updatedItem.getQuantity()) {
                    model.addAttribute("errorMessage", "❌ Insufficient stock for item: " + updatedItem.getItemNo());
                    model.addAttribute("sales", saleRepository.findByType(SaleType.SALE));
                    model.addAttribute("showModal", true);
                    return "add_sale";
                }
                stockItem.setQuantity(stockItem.getQuantity() - updatedItem.getQuantity());
                invoiceItemRepository.save(stockItem);

                // Update fields
                existingItem.setItemNo(updatedItem.getItemNo());
                existingItem.setItemName(updatedItem.getItemName());
                existingItem.setQuantity(updatedItem.getQuantity());
                existingItem.setSellingPrice(updatedItem.getSellingPrice());
                existingItem.setAmount(updatedItem.getAmount());
                updatedItems.add(existingItem);

                existingItemsMap.remove(updatedItem.getId());
            } else {
                // New SaleItem
                if (stockItem.getQuantity() < updatedItem.getQuantity()) {
                    model.addAttribute("errorMessage", "❌ Insufficient stock for item: " + updatedItem.getItemNo());
                    model.addAttribute("sales", saleRepository.findByType(SaleType.SALE));
                    model.addAttribute("showModal", true);
                    return "add_sale";
                }

                stockItem.setQuantity(stockItem.getQuantity() - updatedItem.getQuantity());
                invoiceItemRepository.save(stockItem);

                updatedItems.add(updatedItem);
            }

            totalAmount += updatedItem.getAmount();
        }

        // ✅ Handle deleted items (restore stock)
        for (SaleItem removedItem : existingItemsMap.values()) {
            InvoiceItem stockItem = invoiceItemRepository.findByItemNo(removedItem.getItemNo())
                    .orElse(null);
            if (stockItem != null) {
                stockItem.setQuantity(stockItem.getQuantity() + removedItem.getQuantity());
                invoiceItemRepository.save(stockItem);
            }
        }

        sale.setItems(updatedItems);

        // Apply discount
        double discountPercentage = sale.getDiscountPercentage();
        double discountAmount = (totalAmount * discountPercentage) / 100.0;
        double finalAmount = totalAmount - discountAmount;

        sale.setTotalAmount(totalAmount);
        sale.setDiscountAmount(discountAmount);
        sale.setFinalAmount(finalAmount);

        saleRepository.save(sale);

        model.addAttribute("sales", saleRepository.findByType(SaleType.SALE));
        model.addAttribute("sale", new Sale());
        model.addAttribute("successMessage", (existingSale != null ? "✏️ Sale updated successfully!" : "✅ Sale saved successfully!"));
        return "redirect:/sales/add?success=add";
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
        existingSale.setDiscountPercentage(sale.getDiscountPercentage());
        existingSale.setFinalAmount(sale.getFinalAmount());

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
        return "redirect:/sales/add?success=update";
    }

    @GetMapping("/delete/{id}")
    @Transactional
    public String deleteSale(@PathVariable Long id, Model model) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + id));

        // Restore stock for all sale items
        for (SaleItem item : sale.getItems()) {
            InvoiceItem stockItem = invoiceItemRepository.findByItemNo(item.getItemNo())
                    .orElse(null);
            if (stockItem != null) {
                stockItem.setQuantity(stockItem.getQuantity() + item.getQuantity());
                invoiceItemRepository.save(stockItem);
            }
        }

        // ✅ Delete the sale and all its items
        saleRepository.delete(sale);

        model.addAttribute("sales", saleRepository.findByType(SaleType.SALE)); // 👈 Filter for SALES only
        model.addAttribute("sale", new Sale());
        model.addAttribute("successMessage", "🗑️ Sale deleted successfully!");

        return "redirect:/sales/add?success=delete";
    }


    @GetMapping("/invoice/{id}")
    public String getInvoiceHtml(@PathVariable Long id, Model model) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sale ID: " + id));
        model.addAttribute("sale", sale);
        return "add_sale :: invoiceContent"; // Return only the modal content
    }

    @GetMapping("/reports")
    public String showSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String paymentStatus,
            Model model) {

        // Clean empty strings
        if (clientName != null && clientName.trim().isEmpty()) clientName = null;
        if (paymentStatus != null && paymentStatus.trim().isEmpty()) paymentStatus = null;

        // ✅ Fetch filtered sales (only type=SALE)
        List<Sale> sales = saleRepository.findSalesByFilters(SaleType.SALE, fromDate, toDate, clientName, paymentStatus);
        model.addAttribute("sales", sales);

        // ✅ Metrics: Based on filtered results
        Double totalSales = sales.stream().mapToDouble(Sale::getFinalAmount).sum();
        Double totalDiscounts = sales.stream().mapToDouble(Sale::getDiscountAmount).sum();
        long totalInvoices = sales.size();

        String topItem = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(SaleItem::getItemName, Collectors.summingInt(SaleItem::getQuantity)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalDiscounts", totalDiscounts);
        model.addAttribute("totalInvoices", totalInvoices);
        model.addAttribute("topItem", topItem);

        return "sales_report";
    }


}


