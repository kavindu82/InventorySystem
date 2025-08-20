package com.dem.Inventory.controller;

import com.dem.Inventory.model.Supplier;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.SupplierRepository;
import com.dem.Inventory.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;
    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping("/add")
    public String showSupplierPage(Model model) {
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("supplierList", supplierService.getAllSuppliers());
        return "add_supplier";
    }

    @PostMapping("/save")
    public String saveSupplier(@ModelAttribute("supplier") Supplier supplier, RedirectAttributes redirectAttributes) {
        if (supplierService.isDuplicate(supplier)) {
            redirectAttributes.addFlashAttribute("duplicate", true);
            return "redirect:/supplier/add";
        }

        supplierService.saveSupplier(supplier);
        redirectAttributes.addAttribute("success", "add");
        return "redirect:/supplier/add";
    }

    @GetMapping("/delete/{code}")
    public String deleteSupplier(@PathVariable String code, RedirectAttributes ra) {
        Supplier s = supplierRepository.findById(code).orElse(null);
        if (s == null) {
            ra.addFlashAttribute("error", "❌ Supplier not found.");
            return "redirect:/supplier";
        }

        // block delete if used
        boolean inUse = invoiceItemRepository.existsBySupplierName(s.getSupplierName());
        if (inUse) {
            ra.addFlashAttribute("error", "❌ Cannot delete. Supplier is used in invoice items.");
            return "redirect:/supplier";
        }

        supplierRepository.delete(s);
        ra.addAttribute("success", "delete");
        return "redirect:/supplier/add?success=delete";
    }

    @GetMapping("/check-usage/{code}")
    @ResponseBody
    public Map<String, Boolean> checkSupplierUsage(@PathVariable String code) {
        Supplier s = supplierRepository.findById(code).orElse(null);
        boolean inUse = (s != null) && invoiceItemRepository.existsBySupplierName(s.getSupplierName());
        return Collections.singletonMap("inUse", inUse);
    }


}
