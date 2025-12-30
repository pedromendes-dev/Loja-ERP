package com.example.erp_.dao.impl;

import com.example.erp_.dao.UserDao;
import com.example.erp_.config.GerenciadorBancoDados;
import com.example.erp_.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SqliteUserDao implements UserDao {
    @Override
    public User findByUsername(String username) throws Exception {
        String sql = "SELECT id, username, password_hash, role, active, created_at FROM users WHERE username = ?";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setRole(rs.getString("role"));
                    u.setActive(rs.getInt("active") == 1);
                    return u;
                }
            }
        }
        return null;
    }

    @Override
    public void save(User user) throws Exception {
        String sql = "INSERT INTO users (username, password_hash, role, active, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = GerenciadorBancoDados.obterInstancia().obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        }
    }
}
