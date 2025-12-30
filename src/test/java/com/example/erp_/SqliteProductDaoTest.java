package com.example.erp_;

import com.example.erp_.config.GerenciadorBancoDados;
import com.example.erp_.dao.impl.SqliteProductDao;
import com.example.erp_.model.Product;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteProductDaoTest {
    private Connection realConn;

    @BeforeEach
    public void setup() throws Exception {
        // cria conexão compartilhada em memória do sqlite e carrega o schema
        realConn = DriverManager.getConnection("jdbc:sqlite:file:memdb1?mode=memory&cache=shared");
        // carrega schema.sql via classloader
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db/schema.sql")) {
            assertNotNull(is, "schema.sql não encontrado em resources");
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
        // cria uma conexão proxy cuja close() é no-op para que DAOs que fechem a conexão não destruam o DB em memória
        Connection proxyConn = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("close")) return null; // operação nula
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
    public void testCrudOperations() throws Exception {
        SqliteProductDao dao = new SqliteProductDao();
        Product p = new Product();
        p.setSku("COD123");
        p.setName("Produto Teste");
        p.setDescription("Desc");
        p.setCostPrice(10.0);
        p.setSalePrice(15.5);
        p.setStockQty(20);
        p.setMinStock(5);
        p.setActive(true);

        dao.save(p);
        assertNotNull(p.getId(), "ID deve ser gerado");

        Product fetched = dao.findById(p.getId());
        assertNotNull(fetched);
        assertEquals(p.getSku(), fetched.getSku());

        fetched.setName("Produto Atualizado");
        fetched.setSalePrice(17.0);
        dao.update(fetched);
        Product updated = dao.findById(fetched.getId());
        assertEquals("Produto Atualizado", updated.getName());
        assertEquals(17.0, updated.getSalePrice());

        dao.updateStock(updated.getId(), 3);
        Product afterStock = dao.findById(updated.getId());
        assertEquals(3, afterStock.getStockQty());

        // estoque baixo
        assertTrue(dao.findLowStock().stream().anyMatch(x -> x.getId().equals(updated.getId())));

        dao.delete(updated.getId());
        assertNull(dao.findById(updated.getId()));
    }
}
