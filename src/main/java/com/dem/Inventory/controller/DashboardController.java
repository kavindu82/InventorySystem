package com.dem.Inventory.controller;

import com.dem.Inventory.model.InvoiceItem;
import com.dem.Inventory.repository.InvoiceItemRepository;
import com.dem.Inventory.repository.ItemRepository;
import com.dem.Inventory.repository.SaleRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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

        // ✅ Low stock items list (count and list)
        List<InvoiceItem> AllStockItems = invoiceItemRepository.findAllStockItems();
        model.addAttribute("AllStockItems", AllStockItems != null ? AllStockItems : List.of());

        // ✅ Low stock items (count and list)
        int lowStockCount = invoiceItemRepository.countLowStockItems();
        model.addAttribute("lowStockCount", lowStockCount);

        // ✅ Low stock items list (count and list)
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

    // ✅ Excel download for AllStockItems
    @GetMapping("/all-stock.xlsx")
    public ResponseEntity<byte[]> downloadAllStockExcel() {
        List<InvoiceItem> rows = invoiceItemRepository.findAllStockItems(); // you already use this

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("All Inventory Items");

            // Header
            String[] headers = {
                    "Item No", "Shipping Mark", "Item Name",
                    "Quantity", "Original Cost Price", "Converted Cost Price"
            };
            Row h = sh.createRow(0);
            for (int c = 0; c < headers.length; c++) h.createCell(c).setCellValue(headers[c]);

            // Body
            int r = 1;
            for (InvoiceItem it : rows) {
                Row row = sh.createRow(r++);
                // NOTE: Adjust getters to your InvoiceItem fields
                row.createCell(0).setCellValue(nz(it.getItemNo()));

                // If you have a dedicated "shipping mark" field, use it here.
                // Temporarily using itemName per your current Thymeleaf binding.
                row.createCell(1).setCellValue(nz(it.getItemName()));

                // Your table uses itemType in the "Item Name" column; if that's really the name, keep it.
                // Otherwise, switch to getItemName() as needed.
                row.createCell(2).setCellValue(nz(it.getItemType()));
                row.createCell(3).setCellValue(it.getQuantity());
                row.createCell(4).setCellValue(safeDouble(it.getOriginalCostPrice())); // original
                row.createCell(5).setCellValue(safeDouble(it.getCostPrice()));         // converted (as per your view)
            }

            for (int c = 0; c < headers.length; c++) sh.autoSizeColumn(c);

            byte[] bytes;
            try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
                wb.write(bos);
                bytes = bos.toByteArray();
            }

            HttpHeaders hs = new HttpHeaders();
            hs.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            hs.setContentDisposition(ContentDisposition.attachment().filename("all_stock_items.xlsx").build());

            return new ResponseEntity<>(bytes, hs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static double safeDouble(Double d) { return d == null ? 0.0 : d; }


}
