package com.example.erp_;

import com.example.erp_.config.GerenciadorBancoDados;
import com.example.erp_.model.Product;
import com.example.erp_.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {
    private Connection realConn;

    @BeforeEach
    public void setup() throws Exception {
        realConn = DriverManager.getConnection("jdbc:sqlite:file:memdb1?mode=memory&cache=shared");
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db/schema.sql")) {
            assertNotNull(is, "schema.sql não encontrado");
            String sql = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            try (Statement stmt = realConn.createStatement()) {
                stmt.executeUpdate("PRAGMA foreign_keys = ON;");
                String[] parts = sql.split(";\\s*\n");
                for (String part : parts) {
                    String s = part.trim();
                    if (!s.isEmpty()) stmt.executeUpdate(s);
                }
            }
        }
        Connection proxyConn = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("close")) return null; // no-op
                return method.invoke(realConn, args);
            }
        });
        GerenciadorBancoDados.obterInstancia().definirConexaoDeTeste(proxyConn);
    }

    @AfterEach
    public void teardown() throws Exception {
        if (realConn != null && !realConn.isClosed()) realConn.close();
        GerenciadorBancoDados.reiniciarInstanciaParaTestes();
    }

    @Test
    public void testCreateAndAdjustFlow() throws Exception {
        ProductService service = new ProductService();
        Product p = new Product();
        p.setSku("SVC123");
        p.setName("Serv Produto");
        p.setSalePrice(9.9);
        p.setStockQty(2);
        p.setMinStock(5);
        service.create(p);
        assertNotNull(p.getId());

        // evento de estoque baixo seria disparado; agora ajusta estoque para cima
        service.adjustStock(p.getId(), 10);
        Product after = service.findById(p.getId());
        assertEquals(12, after.getStockQty());

        // tentativa de ajuste inválido (muito negativo)
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.adjustStock(p.getId(), -100));
        assertTrue(ex.getMessage().contains("Quantidade insuficiente") || ex.getMessage().toLowerCase().contains("insuficiente"));

        // update
        after.setName("Nome Novo");
        service.update(after);
        Product updated = service.findById(after.getId());
        assertEquals("Nome Novo", updated.getName());

        // delete
        service.delete(updated.getId());
        assertNull(service.findById(updated.getId()));
    }
}
