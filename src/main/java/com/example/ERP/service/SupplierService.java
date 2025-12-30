package com.example.erp.service;

public class SupplierService {
    private final com.example.erp_.service.SupplierService delegate = new com.example.erp_.service.SupplierService();

    public void create(com.example.erp.model.Supplier s) throws Exception { delegate.create(convertToUnderscore(s)); }
    public void update(com.example.erp.model.Supplier s) throws Exception { delegate.update(convertToUnderscore(s)); }
    public void delete(Integer id) throws Exception { delegate.delete(id); }
    public com.example.erp.model.Supplier findById(Integer id) throws Exception { return convertFromUnderscore(delegate.findById(id)); }
    public java.util.List<com.example.erp.model.Supplier> findAll() throws Exception {
        java.util.List<com.example.erp_.model.Supplier> list = delegate.findAll();
        java.util.List<com.example.erp.model.Supplier> out = new java.util.ArrayList<>();
        for (com.example.erp_.model.Supplier s : list) out.add(convertFromUnderscore(s));
        return out;
    }
    public java.util.List<com.example.erp.model.Supplier> searchByName(String q) throws Exception {
        java.util.List<com.example.erp_.model.Supplier> list = delegate.searchByName(q);
        java.util.List<com.example.erp.model.Supplier> out = new java.util.ArrayList<>();
        for (com.example.erp_.model.Supplier s : list) out.add(convertFromUnderscore(s));
        return out;
    }

    // Converters simples: copiam campos básicos. Ajuste conforme o modelo real.
    private com.example.erp_.model.Supplier convertToUnderscore(com.example.erp.model.Supplier s) {
        if (s == null) return null;
        com.example.erp_.model.Supplier out = new com.example.erp_.model.Supplier();
        out.setId(s.getId()); out.setName(s.getName()); out.setCnpj(s.getCnpj()); out.setEmail(s.getEmail()); out.setContact(s.getContact()); out.setAddress(s.getAddress()); out.setActive(s.isActive());
        return out;
    }

    private com.example.erp.model.Supplier convertFromUnderscore(com.example.erp_.model.Supplier s) {
        if (s == null) return null;
        com.example.erp.model.Supplier out = new com.example.erp.model.Supplier();
        out.setId(s.getId()); out.setName(s.getName()); out.setCnpj(s.getCnpj()); out.setEmail(s.getEmail()); out.setContact(s.getContact()); out.setAddress(s.getAddress()); out.setActive(s.isActive());
        return out;
    }
}

