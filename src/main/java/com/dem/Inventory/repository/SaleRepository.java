package com.dem.Inventory.repository;

import com.dem.Inventory.model.Sale;
import com.dem.Inventory.model.SaleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findTopByOrderByIdDesc();

    List<Sale> findByType(SaleType type);
    @Query("SELECT SUM(s.finalAmount) FROM Sale s")
    Double getTotalSales();

    @Query("SELECT SUM(s.discountAmount) FROM Sale s")
    Double getTotalDiscounts();

    @Query("SELECT s.itemName FROM SaleItem s GROUP BY s.itemName ORDER BY SUM(s.quantity) DESC LIMIT 1")
    String getTopSellingItem();

    @Query("SELECT s FROM Sale s " +
            "WHERE s.type = :type " + // ✅ Filter by type
            "AND (:fromDate IS NULL OR s.saleDate >= :fromDate) " +
            "AND (:toDate IS NULL OR s.saleDate <= :toDate) " +
            "AND (:clientName IS NULL OR LOWER(s.clientName) LIKE LOWER(CONCAT('%', :clientName, '%'))) " +
            "AND (:paymentStatus IS NULL OR s.paymentStatus = :paymentStatus)")
    List<Sale> findSalesByFilters(@Param("type") SaleType type,
                                  @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate,
                                  @Param("clientName") String clientName,
                                  @Param("paymentStatus") String paymentStatus);

    @Query("SELECT s.invoiceNumber FROM Sale s WHERE s.type = 'QUOTATION' ORDER BY s.id DESC LIMIT 1")
    String findLastQuotationNumber();

    @Query("SELECT SUM(s.finalAmount) FROM Sale s WHERE s.saleDate = :date AND s.type = 'SALE'")
    Double totalSalesToday(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT s.clientName) FROM Sale s WHERE s.saleDate = :date AND s.type = 'SALE'")
    long countClientsBilledToday(@Param("date") LocalDate date);

    @Query("SELECT i.itemName, SUM(i.quantity) FROM Sale s JOIN s.items i " +
            "WHERE s.type = 'SALE' GROUP BY i.itemName ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findTopSellingItems();

    @Query("SELECT s.saleDate, SUM(s.finalAmount) FROM Sale s " +
            "WHERE s.saleDate >= :startDate AND s.type = 'SALE' GROUP BY s.saleDate ORDER BY s.saleDate ASC")
    List<Object[]> findSalesTrendLast7Days(@Param("startDate") LocalDate startDate);


}

