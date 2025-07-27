package com.dem.Inventory.controller;

import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.ItemRepository;
import com.dem.Inventory.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SaleRepository saleRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {

        // ✅ Total Inventory Items
        int totalItems = (int) itemRepository.count();
        model.addAttribute("totalItems", totalItems);

        // ✅ Low stock items (count and list)
        int lowStockCount = invoiceItemRepository.countLowStockItems();
        model.addAttribute("lowStockCount", lowStockCount);

        List<InvoiceItem> lowStockItems = invoiceItemRepository.findLowStockItems();
        model.addAttribute("lowStockItems", lowStockItems != null ? lowStockItems : List.of());

        // ✅ Total sales today
        Double salesToday = saleRepository.totalSalesToday(LocalDate.now());
        model.addAttribute("salesToday", salesToday != null ? salesToday : 0);

        // ✅ Clients billed today
        long clientsToday = saleRepository.countClientsBilledToday(LocalDate.now());
        model.addAttribute("clientsToday", clientsToday);

        // ✅ Top Selling Items (fallback if empty)
        List<Object[]> topItems = saleRepository.findTopSellingItems();
        model.addAttribute("topItems", topItems != null ? topItems : List.of());

        // ✅ Sales Trend (last 7 days, fallback if empty)
        LocalDate startDate = LocalDate.now().minusDays(6);
        List<Object[]> salesTrend = saleRepository.findSalesTrendLast7Days(startDate);
        model.addAttribute("salesTrend", salesTrend != null ? salesTrend : List.of());

        return "dashboard";
    }
}
