package com.example.erp_.builder;

import com.example.erp_.model.Client;
import java.time.LocalDateTime;

public class ClientBuilder {
    private Client client;

    public ClientBuilder() {
        client = new Client();
    }

    public ClientBuilder id(Integer id) {
        client.setId(id);
        return this;
    }

    public ClientBuilder name(String name) {
        client.setName(name);
        return this;
    }

    public ClientBuilder cpfCnpj(String cpfCnpj) {
        client.setCpfCnpj(cpfCnpj);
        return this;
    }

    public ClientBuilder email(String email) {
        client.setEmail(email);
        return this;
    }

    public ClientBuilder phone(String phone) {
        client.setPhone(phone);
        return this;
    }

    public ClientBuilder address(String address) {
        client.setAddress(address);
        return this;
    }

    public ClientBuilder active(boolean active) {
        client.setActive(active);
        return this;
    }

    public ClientBuilder createdAt(LocalDateTime createdAt) {
        client.setCreatedAt(createdAt);
        return this;
    }

    public Client build() {
        if (client.getCreatedAt() == null) {
            client.setCreatedAt(LocalDateTime.now());
        }
        return client;
    }
}

