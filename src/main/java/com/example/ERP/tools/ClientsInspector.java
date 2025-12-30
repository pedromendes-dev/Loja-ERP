package com.example.ERP.tools;

import com.example.erp_.service.ClientService;

public class ClientsInspector {
    public static void main(String[] args) throws Exception {
        ClientService svc = new ClientService();
        java.util.List<com.example.erp_.model.Client> list = svc.findAll();
        System.out.println("Total clients: " + list.size());
        int lim = Math.min(list.size(), 10);
        for (int i=0;i<lim;i++) {
            com.example.erp_.model.Client c = list.get(i);
            System.out.println(String.format("%d: %s <%s> %s", c.getId(), c.getName(), c.getEmail(), c.getCpfCnpj()));
        }
    }
}

