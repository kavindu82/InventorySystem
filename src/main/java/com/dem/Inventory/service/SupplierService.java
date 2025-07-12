package com.dem.Inventory.service;

import com.dem.Inventory.model.Supplier;
import com.dem.Inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public void saveSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    public void deleteSupplierByCode(String code) {
        supplierRepository.deleteById(code);
    }

    public boolean isDuplicate(Supplier supplier) {
        return supplierRepository.findAll().stream().anyMatch(existing ->
                !existing.getSupplierCode().equals(supplier.getSupplierCode()) &&
                        existing.getSupplierName().equalsIgnoreCase(supplier.getSupplierName()) &&
                        existing.getSupplierAddress().equalsIgnoreCase(supplier.getSupplierAddress())
        );
    }
}
