package com.example.erp_;

import com.example.erp_.config.GerenciadorBancoDados;
import com.example.erp_.model.Product;
import com.example.erp_.service.ProductService;
import com.example.erp_.service.sales.SaleItem;
import com.example.erp_.service.sales.SalesService;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SalesServiceTest {
    private Connection realConn;

    @BeforeEach
    public void setup() throws Exception {
        // shared in-memory DB for this test
        realConn = DriverManager.getConnection("jdbc:sqlite:file:memdb_sales?mode=memory&cache=shared");
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db/schema.sql")) {
            assertNotNull(is, "schema.sql not found");
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
                if (method.getName().equals("close")) return null;
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
    public void testPerformSaleSuccess() throws Exception {
        ProductService ps = new ProductService();
        Product p = new Product();
        p.setSku("SALE-001");
        p.setName("Produto Venda");
        p.setSalePrice(5.0);
        p.setStockQty(10);
        p.setMinStock(1);
        ps.create(p);
        assertNotNull(p.getId());

        SalesService sales = new SalesService();
        SaleItem item = new SaleItem(p.getId(), 3);
        sales.performSale(List.of(item));

        Product after = ps.findById(p.getId());
        assertEquals(7, after.getStockQty());
    }

    @Test
    public void testPerformSaleInsufficientDoesNotChangeStock() throws Exception {
        ProductService ps = new ProductService();
        Product p = new Product();
        p.setSku("SALE-002");
        p.setName("Produto Venda2");
        p.setSalePrice(7.0);
        p.setStockQty(2);
        p.setMinStock(0);
        ps.create(p);

        SalesService sales = new SalesService();
        SaleItem item = new SaleItem(p.getId(), 5);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> sales.performSale(List.of(item)));
        assertTrue(ex.getMessage().toLowerCase().contains("insuficiente") || ex.getMessage().toLowerCase().contains("não encontrado") || ex.getMessage().toLowerCase().contains("nao"));

        Product after = ps.findById(p.getId());
        assertEquals(2, after.getStockQty(), "Stock should remain unchanged after failed sale");
    }
}
