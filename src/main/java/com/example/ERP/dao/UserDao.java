package com.example.erp_.dao;

import com.example.erp_.model.User;

public interface UserDao {
    User findByUsername(String username) throws Exception;
    void save(User user) throws Exception;
}

