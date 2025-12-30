package com.example.erp_.dao;

import com.example.erp_.model.Supplier;
import java.util.List;

public interface SupplierDao {
    void save(Supplier supplier) throws Exception;
    void update(Supplier supplier) throws Exception;
    void delete(Integer id) throws Exception;
    Supplier findById(Integer id) throws Exception;
    List<Supplier> findAll() throws Exception;
    List<Supplier> searchByName(String name) throws Exception;
}

