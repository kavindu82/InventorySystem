package com.dem.Inventory.controller;

import com.dem.Inventory.model.Supplier;
import com.dem.Inventory.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

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
    public String deleteSupplier(@PathVariable String code) {
        supplierService.deleteSupplierByCode(code);
        return "redirect:/supplier/add?success=delete";
    }

}
