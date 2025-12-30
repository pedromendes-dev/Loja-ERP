package com.example.erp_.service;

import com.example.erp_.dao.impl.SqliteProductDao;
import com.example.erp_.model.Product;
import com.example.erp_.patterns.observer.EventBus;

import java.util.List;

public class ProductService {
    private SqliteProductDao productDao = new SqliteProductDao();

    // Cria um produto, valida nome obrigatório e publica eventos relevantes
    public void create(Product p) throws Exception {
        if (p.getName() == null || p.getName().trim().isEmpty()) throw new IllegalArgumentException("Nome do produto é obrigatório");
        productDao.save(p);
        EventBus.getInstance().publish("product.created", p);
        if (p.getStockQty() <= p.getMinStock()) EventBus.getInstance().publish("product.lowstock", p);
    }

    // Atualiza um produto existente
public void update(Product p) throws Exception {
    if (p.getId() == null) throw new IllegalArgumentException("ID obrigatório");
    productDao.update(p);
    EventBus.getInstance().publish("product.updated", p);
    if (p.getStockQty() <= p.getMinStock()) EventBus.getInstance().publish("product.lowstock", p);
}

public void delete(Integer id) throws Exception { productDao.delete(id); EventBus.getInstance().publish("product.deleted", id); }

public Product findById(Integer id) throws Exception { return productDao.findById(id); }

public List<Product> findAll() throws Exception { return productDao.findAll(); }

public List<Product> findLowStock() throws Exception { return productDao.findLowStock(); }

// Ajusta estoque e publica eventos de alteração de estoque/estoque baixo
public void adjustStock(Integer productId, int delta) throws Exception {
    Product p = productDao.findById(productId);
    if (p == null) throw new IllegalArgumentException("Produto não encontrado");
    int newQty = p.getStockQty() + delta;
    if (newQty < 0) throw new IllegalArgumentException("Quantidade insuficiente");
    productDao.updateStock(productId, newQty);
    p.setStockQty(newQty);
    EventBus.getInstance().publish("product.stockchanged", p);
    if (newQty <= p.getMinStock()) EventBus.getInstance().publish("product.lowstock", p);
}

// Busca no servidor por nome ou código (insensível a maiúsculas/minúsculas), delega para o DAO
public List<Product> search(String term) throws Exception {
    if (term == null) term = "";
    return productDao.findByTerm(term.trim());
}
}
