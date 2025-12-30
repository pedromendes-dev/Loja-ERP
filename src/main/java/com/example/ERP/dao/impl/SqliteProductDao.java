package com.example.erp_.dao.impl;

import com.example.erp_.dao.ProductDao;
import com.example.erp_.config.GerenciadorBancoDados;
import com.example.erp_.model.Product;
import com.example.erp_.utils.DateTimeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteProductDao implements ProductDao {
    @Override
    public void save(Product p) throws Exception {
        String sql = "INSERT INTO products (sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock, active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getSku());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            if (p.getCategoryId() == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, p.getCategoryId());
            if (p.getSupplierId() == null) ps.setNull(5, java.sql.Types.INTEGER); else ps.setInt(5, p.getSupplierId());
            ps.setDouble(6, p.getCostPrice());
            ps.setDouble(7, p.getSalePrice());
            ps.setInt(8, p.getStockQty());
            ps.setInt(9, p.getMinStock());
            ps.setInt(10, p.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) p.setId(rs.getInt(1)); }
        }
    }

    @Override
    public void update(Product p) throws Exception {
        String sql = "UPDATE products SET sku=?, name=?, description=?, category_id=?, supplier_id=?, cost_price=?, sale_price=?, stock_qty=?, min_stock=?, active=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getSku());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            if (p.getCategoryId() == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, p.getCategoryId());
            if (p.getSupplierId() == null) ps.setNull(5, java.sql.Types.INTEGER); else ps.setInt(5, p.getSupplierId());
            ps.setDouble(6, p.getCostPrice());
            ps.setDouble(7, p.getSalePrice());
            ps.setInt(8, p.getStockQty());
            ps.setInt(9, p.getMinStock());
            ps.setInt(10, p.isActive() ? 1 : 0);
            ps.setInt(11, p.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Product findById(Integer id) throws Exception {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapRow(rs); }
        }
        return null;
    }

    @Override
    public List<Product> findAll() throws Exception {
        String sql = "SELECT * FROM products ORDER BY name";
        List<Product> list = new ArrayList<>();
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        return list;
    }

    @Override
    public List<Product> findLowStock() throws Exception {
        String sql = "SELECT * FROM products WHERE stock_qty <= min_stock";
        List<Product> list = new ArrayList<>();
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        return list;
    }

    @Override
    public void updateStock(Integer productId, int newQty) throws Exception {
        String sql = "UPDATE products SET stock_qty = ?, updated_at=CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQty);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    // Nova implementação: busca por termo (name ou sku), com escape para LIKE
    public List<Product> findByTerm(String term) throws Exception {
        String normalized = term == null ? "" : term.trim();
        // escape % and _ characters for LIKE
        String escaped = normalized.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        String like = "%" + escaped + "%";
        String sql = "SELECT * FROM products WHERE (LOWER(name) LIKE LOWER(?) ESCAPE '\\' OR LOWER(sku) LIKE LOWER(?) ESCAPE '\\') ORDER BY name";
        List<Product> list = new ArrayList<>();
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        }
        return list;
    }

    private Product mapRow(ResultSet rs) throws Exception {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setCategoryId(rs.getObject("category_id") != null ? rs.getInt("category_id") : null);
        p.setSupplierId(rs.getObject("supplier_id") != null ? rs.getInt("supplier_id") : null);
        p.setCostPrice(rs.getDouble("cost_price"));
        p.setSalePrice(rs.getDouble("sale_price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setMinStock(rs.getInt("min_stock"));
        p.setActive(rs.getInt("active") == 1);
        p.setCreatedAt(rs.getString("created_at") != null ? DateTimeUtils.parseFlexible(rs.getString("created_at")) : null);
        p.setUpdatedAt(rs.getString("updated_at") != null ? DateTimeUtils.parseFlexible(rs.getString("updated_at")) : null);
        return p;
    }
}
