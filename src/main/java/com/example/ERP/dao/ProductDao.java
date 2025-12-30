package com.example.erp_.dao;

import com.example.erp_.model.Product;
import java.util.List;

public interface ProductDao {
    void save(Product p) throws Exception;
    void update(Product p) throws Exception;
    void delete(Integer id) throws Exception;
    Product findById(Integer id) throws Exception;
    List<Product> findAll() throws Exception;
    List<Product> findLowStock() throws Exception;
    void updateStock(Integer productId, int newQty) throws Exception;
}

