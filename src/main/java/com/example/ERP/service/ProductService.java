package com.example.erp.service;

import com.example.erp_.dao.impl.SqliteProductDao;
import com.example.erp_.patterns.observer.EventBus;
import com.example.erp.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private SqliteProductDao productDao = new SqliteProductDao();

    public void create(Product p) throws Exception {
        if (p.getName() == null || p.getName().trim().isEmpty()) throw new IllegalArgumentException("Nome do produto é obrigatório");
        // converte para o modelo underscore e salva
        com.example.erp_.model.Product up = toUnderscore(p);
        productDao.save(up);
        EventBus.getInstance().publish("product.created", p);
        if (p.getStockQty() <= p.getMinStock()) EventBus.getInstance().publish("product.lowstock", p);
    }

    public void update(Product p) throws Exception {
        if (p.getId() == null) throw new IllegalArgumentException("ID obrigatório");
        com.example.erp_.model.Product up = toUnderscore(p);
        productDao.update(up);
        EventBus.getInstance().publish("product.updated", p);
        if (p.getStockQty() <= p.getMinStock()) EventBus.getInstance().publish("product.lowstock", p);
    }

    public void delete(Integer id) throws Exception { productDao.delete(id); EventBus.getInstance().publish("product.deleted", id); }

    public Product findById(Integer id) throws Exception { return fromUnderscore(productDao.findById(id)); }

    public List<Product> findAll() throws Exception {
        List<com.example.erp_.model.Product> list = productDao.findAll();
        List<Product> out = new ArrayList<>();
        for (com.example.erp_.model.Product up : list) out.add(fromUnderscore(up));
        return out;
    }

    public List<Product> findLowStock() throws Exception {
        List<com.example.erp_.model.Product> list = productDao.findLowStock();
        List<Product> out = new ArrayList<>();
        for (com.example.erp_.model.Product up : list) out.add(fromUnderscore(up));
        return out;
    }

    public void adjustStock(Integer productId, int delta) throws Exception {
        com.example.erp_.model.Product up = productDao.findById(productId);
        if (up == null) throw new IllegalArgumentException("Produto não encontrado");
        int newQty = up.getStockQty() + delta;
        if (newQty < 0) throw new IllegalArgumentException("Quantidade insuficiente");
        productDao.updateStock(productId, newQty);
        // publica usando o modelo canônico
        Product p = fromUnderscore(up);
        p.setStockQty(newQty);
        EventBus.getInstance().publish("product.stockchanged", p);
        if (newQty <= p.getMinStock()) EventBus.getInstance().publish("product.lowstock", p);
    }

    // Busca no servidor por nome ou código (insensível a maiúsculas/minúsculas), delega para o DAO
    public List<Product> search(String term) throws Exception {
        if (term == null) term = "";
        List<com.example.erp_.model.Product> list = productDao.findByTerm(term.trim());
        List<Product> out = new ArrayList<>();
        for (com.example.erp_.model.Product up : list) out.add(fromUnderscore(up));
        return out;
    }

    private com.example.erp_.model.Product toUnderscore(Product p) {
        if (p == null) return null;
        com.example.erp_.model.Product up = new com.example.erp_.model.Product();
        up.setId(p.getId()); up.setSku(p.getSku()); up.setName(p.getName()); up.setDescription(p.getDescription()); up.setCategoryId(p.getCategoryId()); up.setSupplierId(p.getSupplierId()); up.setCostPrice(p.getCostPrice()); up.setSalePrice(p.getSalePrice()); up.setStockQty(p.getStockQty()); up.setMinStock(p.getMinStock()); up.setActive(p.isActive());
        return up;
    }

    private Product fromUnderscore(com.example.erp_.model.Product up) {
        if (up == null) return null;
        Product p = new Product();
        p.setId(up.getId()); p.setSku(up.getSku()); p.setName(up.getName()); p.setDescription(up.getDescription()); p.setCategoryId(up.getCategoryId()); p.setSupplierId(up.getSupplierId()); p.setCostPrice(up.getCostPrice()); p.setSalePrice(up.getSalePrice()); p.setStockQty(up.getStockQty()); p.setMinStock(up.getMinStock()); p.setActive(up.isActive());
        return p;
    }
}
