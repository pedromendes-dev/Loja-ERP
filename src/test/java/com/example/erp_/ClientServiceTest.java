package com.example.erp_;

import com.example.erp_.config.GerenciadorBancoDados;
import com.example.erp_.model.Client;
import com.example.erp_.service.ClientService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ClientServiceTest {
    private static ClientService service = new ClientService();

    @BeforeAll
    public static void init() throws Exception {
        GerenciadorBancoDados.obterInstancia().inicializar();
    }

    @Test
    public void testCreateUpdateDeleteClient() throws Exception {
        Client c = new Client();
        c.setName("Test Client");
        c.setEmail("test@example.com");
        service.create(c);
        Assertions.assertNotNull(c.getId());

        // update
        c.setName("Test Client Edited");
        service.update(c);
        Client fromDb = service.findById(c.getId());
        Assertions.assertEquals("Test Client Edited", fromDb.getName());

        // search
        List<Client> list = service.searchByName("Test Client Edited");
        Assertions.assertFalse(list.isEmpty());

        // delete
        service.delete(c.getId());
        Client after = service.findById(c.getId());
        Assertions.assertNull(after);
    }

    @AfterAll
    public static void cleanup() throws Exception {
        // nothing specific; DB remains for manual inspection
    }
}
