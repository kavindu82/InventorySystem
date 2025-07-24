package com.dem.Inventory.controller;

import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.ItemRepository;
import com.dem.Inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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
        try {
            // ✅ Convert to LKR if needed
            if ("LKR".equalsIgnoreCase(invoiceItem.getCurrency())) {
                invoiceItem.setCostPrice(invoiceItem.getOriginalCostPrice());
            } else {
                double convertedPrice = fetchConvertedPrice(invoiceItem.getCurrency(), invoiceItem.getOriginalCostPrice());
                invoiceItem.setCostPrice(convertedPrice);
            }

            // ✅ Calculate amount
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

    private double fetchConvertedPrice(String currency, double amount) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://api.exchangerate.host/latest?base=" + currency + "&symbols=LKR";

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body != null && body.containsKey("rates")) {
                    Map<String, Object> ratesMap = (Map<String, Object>) body.get("rates");
                    if (ratesMap != null && ratesMap.containsKey("LKR")) {
                        Double rate = Double.parseDouble(ratesMap.get("LKR").toString());
                        return amount * rate;
                    } else {
                        System.err.println("⚠️ API response missing LKR rate: " + body);
                        throw new RuntimeException("Exchange rate for LKR not found.");
                    }
                } else {
                    System.err.println("⚠️ Invalid API response: " + body);
                    throw new RuntimeException("Invalid response from currency API.");
                }
            } else {
                System.err.println("⚠️ API call failed: HTTP " + response.getStatusCode());
                throw new RuntimeException("Failed to fetch exchange rate");
            }
        } catch (Exception ex) {
            System.err.println("⚠️ Currency API error: " + ex.getMessage());
            // Fallback to default rate
            double fallbackRate = 300.0; // Example: 1 USD = 300 LKR
            System.out.println("✅ Using fallback rate: " + fallbackRate);
            return amount * fallbackRate;
        }
    }


}

