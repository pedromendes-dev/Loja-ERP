package com.example.ERP.tools;

import com.example.erp_.model.Client;
import com.example.erp_.service.ClientService;

public class ClientsPopulator {
    public static void main(String[] args) throws Exception {
        int n = 100;
        if (args.length>0) try { n = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        ClientService svc = new ClientService();
        int created = 0;
        for (int i=1;i<=n;i++) {
            Client c = new Client();
            c.setName("Cliente Gerado " + i);
            c.setCpfCnpj(String.format("%011d", 50000000000L + i));
            c.setEmail("gerado" + i + "@ex.com");
            c.setPhone("(11) 9" + String.format("%04d-%04d", i, i));
            c.setAddress("Rua Gerada " + i);
            try { svc.create(c); created++; } catch (Exception e) { /* ignore duplicates */ }
        }
        System.out.println("Created clients: " + created);
    }
}

