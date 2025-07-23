package com.dem.Inventory.controller;

import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.model.Sale;
import com.dem.Inventory.model.SaleItem;
import com.dem.Inventory.model.SaleType;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.SaleRepository;
import com.dem.Inventory.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/quotation")
public class QuotationController {

    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private InvoiceItemRepository invoiceItemRepository;
    @Autowired
    private QuotationService quotationService;


    // ✅ 1. Load Quotation Page
    @GetMapping
    public String showQuotationPage(Model model) {
        model.addAttribute("sale", new Sale()); // For the Add Quotation form
        model.addAttribute("quotations", saleRepository.findByType(SaleType.QUOTATION)); // Load saved quotations
        return "quotation"; // Renders quotation.html
    }

    // ✅ 2. Save Quotation
    @PostMapping("/save")
    public String saveQuotation(@ModelAttribute("sale") Sale quotation) {
        quotation.setType(SaleType.QUOTATION); // Force type
        quotation.setInvoiceNumber(generateQuotationNumber()); // Unique Q001, Q002
        quotation.setPaymentStatus("QUTN");

        // Set sale reference in each SaleItem
        quotation.getItems().forEach(item -> item.setSale(quotation));

        saleRepository.save(quotation);
        return "redirect:/quotation";
    }

    // ✅ 3. View Quotation Details for Modal
    @GetMapping("/view/{id}")
    public String viewQuotationDetails(@PathVariable Long id, Model model) {
        Sale quotation = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Quotation ID: " + id));
        model.addAttribute("sale", quotation);
        return "fragments/quotationView :: invoiceContent";
    }


    private String generateQuotationNumber() {
        // Fetch last quotation number
        String lastQuotationNumber = saleRepository.findLastQuotationNumber();

        if (lastQuotationNumber == null) {
            return "Q001"; // Start with Q001
        } else {
            // Strip "Q" prefix and increment
            int next = Integer.parseInt(lastQuotationNumber.substring(1)) + 1;
            return String.format("Q%03d", next);
        }
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> downloadQuotationPdf(@PathVariable Long id) {
        byte[] pdf = quotationService.generateQuotationPdf(id); // Generate PDF bytes
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "quotation_" + id + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/convert/{id}")
    public String convertToSale(@PathVariable Long id) {
        Sale quotation = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid quotation ID"));

        quotation.setType(SaleType.SALE);

        // Deduct stock for all items
        for (SaleItem item : quotation.getItems()) {
            InvoiceItem stockItem = invoiceItemRepository.findByItemNo(item.getItemNo())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + item.getItemNo()));
            if (stockItem.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for item: " + item.getItemNo());
            }
            stockItem.setQuantity(stockItem.getQuantity() - item.getQuantity());
            invoiceItemRepository.save(stockItem);
        }

        saleRepository.save(quotation);
        return "redirect:/sales/add"; // Go to Add Sale page
    }


}
