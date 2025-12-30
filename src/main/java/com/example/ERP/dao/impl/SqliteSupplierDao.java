package com.example.erp_.dao.impl;

import com.example.erp_.dao.SupplierDao;
import com.example.erp_.config.DbManager;
import com.example.erp_.model.Supplier;
import com.example.erp_.utils.DateTimeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteSupplierDao implements SupplierDao {
    @Override
    public void save(Supplier supplier) throws Exception {
        String sql = "INSERT INTO suppliers (name, contact, cnpj, email, address, active, created_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DbManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContact());
            ps.setString(3, supplier.getCnpj());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setInt(6, supplier.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) supplier.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public void update(Supplier supplier) throws Exception {
        String sql = "UPDATE suppliers SET name=?, contact=?, cnpj=?, email=?, address=?, active=?, created_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DbManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContact());
            ps.setString(3, supplier.getCnpj());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setInt(6, supplier.isActive() ? 1 : 0);
            ps.setInt(7, supplier.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (Connection conn = DbManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Supplier findById(Integer id) throws Exception {
        String sql = "SELECT id, name, contact, cnpj, email, address, active, created_at FROM suppliers WHERE id = ?";
        try (Connection conn = DbManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Supplier> findAll() throws Exception {
        String sql = "SELECT id, name, contact, cnpj, email, address, active, created_at FROM suppliers ORDER BY name";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DbManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public List<Supplier> searchByName(String name) throws Exception {
        String sql = "SELECT id, name, contact, cnpj, email, address, active, created_at FROM suppliers WHERE name LIKE ? ORDER BY name";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DbManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Supplier mapRow(ResultSet rs) throws Exception {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setContact(rs.getString("contact"));
        s.setCnpj(rs.getString("cnpj"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setActive(rs.getInt("active") == 1);
        s.setCreatedAt(rs.getString("created_at") != null ? DateTimeUtils.parseFlexible(rs.getString("created_at")) : null);
        return s;
    }
}
