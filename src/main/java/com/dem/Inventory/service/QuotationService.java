package com.dem.Inventory.service;

import com.dem.Inventory.model.Sale;
import com.dem.Inventory.model.SaleItem;
import com.dem.Inventory.repository.SaleRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
@Service
public class QuotationService {

    @Autowired
    private SaleRepository saleRepository;

    public byte[] generateQuotationPdf(Long id) {
        Sale quotation = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Quotation ID: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Quotation: " + quotation.getInvoiceNumber(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Empty line

            // Add client details
            document.add(new Paragraph("Client Name: " + quotation.getClientName()));
            document.add(new Paragraph("Client Contact: " + quotation.getClientContact()));
            document.add(new Paragraph("Date: " + quotation.getSaleDate()));
            document.add(new Paragraph(" ")); // Empty line

            // Add table of items
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("Item No");
            table.addCell("Item Name");
            table.addCell("Quantity");
            table.addCell("Price");
            table.addCell("Amount");

            for (SaleItem item : quotation.getItems()) {
                table.addCell(item.getItemNo());
                table.addCell(item.getItemName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(String.format("%.2f", item.getSellingPrice()));
                table.addCell(String.format("%.2f", item.getAmount()));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Add totals
            document.add(new Paragraph("Total Amount: Rs. " + quotation.getTotalAmount()));
            document.add(new Paragraph("Discount (" + quotation.getDiscountPercentage() + "%): Rs. " + quotation.getDiscountAmount()));
            document.add(new Paragraph("Final Amount: Rs. " + quotation.getFinalAmount()));

            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }
}
