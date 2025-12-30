package com.example.erp_.dao;

import com.example.erp_.model.Client;
import java.util.List;

public interface ClientDao {
    void save(Client client) throws Exception;
    void update(Client client) throws Exception;
    void delete(Integer id) throws Exception;
    Client findById(Integer id) throws Exception;
    List<Client> findAll() throws Exception;
    List<Client> searchByName(String name) throws Exception;
}

