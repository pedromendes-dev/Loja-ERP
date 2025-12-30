package com.example.erp_.service;

import com.example.erp_.dao.impl.SqliteSupplierDao;
import com.example.erp_.model.Supplier;

import java.util.List;

public class SupplierService {
    private final SqliteSupplierDao dao = new SqliteSupplierDao();

    public void create(Supplier s) throws Exception { dao.save(s); }
    public void update(Supplier s) throws Exception { dao.update(s); }
    public void delete(Integer id) throws Exception { dao.delete(id); }
    public Supplier findById(Integer id) throws Exception { return dao.findById(id); }
    public List<Supplier> findAll() throws Exception { return dao.findAll(); }
    public List<Supplier> searchByName(String q) throws Exception { return dao.searchByName(q); }
}

