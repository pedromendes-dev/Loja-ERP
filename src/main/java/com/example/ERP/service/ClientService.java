package com.example.erp_.service;

import com.example.erp_.dao.impl.SqliteClientDao;
import com.example.erp_.model.Client;
import com.example.erp_.patterns.observer.EventBus;

import java.util.List;

public class ClientService {
    private SqliteClientDao clientDao = new SqliteClientDao();

    public void create(Client client) throws Exception {
        // basic validations
        if (client.getName() == null || client.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório");
        }
        client.setActive(true);
        clientDao.save(client);
        EventBus.getInstance().publish("client.created", client);
    }

    public void update(Client client) throws Exception {
        if (client.getId() == null) throw new IllegalArgumentException("ID é obrigatório para atualização");
        clientDao.update(client);
        EventBus.getInstance().publish("client.updated", client);
    }

    public void delete(Integer id) throws Exception {
        clientDao.delete(id);
        EventBus.getInstance().publish("client.deleted", id);
    }

    public Client findById(Integer id) throws Exception {
        return clientDao.findById(id);
    }

    public List<Client> findAll() throws Exception {
        return clientDao.findAll();
    }

    public List<Client> searchByName(String name) throws Exception {
        return clientDao.searchByName(name);
    }
}
