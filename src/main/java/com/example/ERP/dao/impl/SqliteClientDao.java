package com.example.erp_.dao.impl;

import com.example.erp_.dao.ClientDao;
import com.example.erp_.config.GerenciadorBancoDados;
import com.example.erp_.model.Client;
import com.example.erp_.utils.DateTimeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteClientDao implements ClientDao {

    @Override
    public void save(Client client) throws Exception {
        String sql = "INSERT INTO clients (name, cpf_cnpj, email, phone, address, active, created_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getCpfCnpj());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getPhone());
            ps.setString(5, client.getAddress());
            ps.setInt(6, client.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    client.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Client client) throws Exception {
        String sql = "UPDATE clients SET name=?, cpf_cnpj=?, email=?, phone=?, address=?, active=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getCpfCnpj());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getPhone());
            ps.setString(5, client.getAddress());
            ps.setInt(6, client.isActive() ? 1 : 0);
            ps.setInt(7, client.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Client findById(Integer id) throws Exception {
        String sql = "SELECT id, name, cpf_cnpj, email, phone, address, active, created_at, updated_at FROM clients WHERE id = ?";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Client> findAll() throws Exception {
        String sql = "SELECT id, name, cpf_cnpj, email, phone, address, active, created_at, updated_at FROM clients ORDER BY name";
        List<Client> list = new ArrayList<>();
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<Client> searchByName(String name) throws Exception {
        String sql = "SELECT id, name, cpf_cnpj, email, phone, address, active, created_at, updated_at FROM clients WHERE name LIKE ? ORDER BY name";
        List<Client> list = new ArrayList<>();
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Client mapRow(ResultSet rs) throws Exception {
        Client c = new Client();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setCpfCnpj(rs.getString("cpf_cnpj"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setActive(rs.getInt("active") == 1);
        c.setCreatedAt(rs.getString("created_at") != null ? DateTimeUtils.parseFlexible(rs.getString("created_at")) : null);
        c.setUpdatedAt(rs.getString("updated_at") != null ? DateTimeUtils.parseFlexible(rs.getString("updated_at")) : null);
        return c;
    }
}
