package com.example.erp_.service.sales;

import com.example.erp_.config.DbManager;
import com.example.erp_.model.Product;
import com.example.erp_.patterns.observer.EventBus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço de vendas: persiste um registro de venda e itens da venda, e decrementa o estoque.
 * A operação completa é executada dentro de uma transação JDBC: se qualquer etapa falhar,
 * as alterações no banco de dados são revertidas.
 */
public class ServicoVendas {
    // remova uso de ProductService para garantir uso da mesma conexão/transação

    /**
     * Executa a venda de forma transacional: persiste venda + itens e decrementa o estoque.
     */
    public void realizarVenda(List<ItemVenda> itens) throws Exception {
        if (itens == null || itens.isEmpty()) throw new IllegalArgumentException("Itens da venda não informados");

        Connection conn = DbManager.getInstance().getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // valida disponibilidade e calcula o total utilizando a mesma conexão
            double totalVenda = 0.0;
            Map<Integer, Product> produtosCache = new HashMap<>();
            String selectSql = "SELECT id, name, sale_price, stock_qty, min_stock FROM products WHERE id = ?";
            try (PreparedStatement psSel = conn.prepareStatement(selectSql)) {
                for (ItemVenda item : itens) {
                    psSel.setInt(1, item.getProdutoId());
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Produto não encontrado: " + item.getProdutoId());
                        Product produto = new Product();
                        produto.setId(rs.getInt("id"));
                        produto.setName(rs.getString("name"));
                        produto.setSalePrice(rs.getDouble("sale_price"));
                        produto.setStockQty(rs.getInt("stock_qty"));
                        produto.setMinStock(rs.getInt("min_stock"));
                        produtosCache.put(produto.getId(), produto);
                        if (produto.getStockQty() < item.getQuantidade()) throw new IllegalArgumentException("Quantidade insuficiente para produto: " + produto.getName());
                        totalVenda += produto.getSalePrice() * item.getQuantidade();
                    }
                }
            }

            // insere venda
            String numeroVenda = "SN" + System.currentTimeMillis();
            String insertSaleSql = "INSERT INTO sales (sale_number, total_amount, paid, status) VALUES (?, ?, ?, ?)";
            int vendaId;
            try (PreparedStatement ps = conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, numeroVenda);
                ps.setDouble(2, totalVenda);
                ps.setInt(3, 1); // pago
                ps.setString(4, "COMPLETED");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) vendaId = rs.getInt(1); else throw new IllegalStateException("Falha ao recuperar id da venda");
                }
            }

            // insere itens da venda e decrementa o estoque usando a mesma conexão
            String insertItemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, discount) VALUES (?, ?, ?, ?, ?)";
            String updateStockSql = "UPDATE products SET stock_qty = stock_qty - ?, updated_at=CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql);
                 PreparedStatement psUpdate = conn.prepareStatement(updateStockSql)) {
                for (ItemVenda item : itens) {
                    Product produto = produtosCache.get(item.getProdutoId());
                    psItem.setInt(1, vendaId);
                    psItem.setInt(2, item.getProdutoId());
                    psItem.setInt(3, item.getQuantidade());
                    psItem.setDouble(4, produto.getSalePrice());
                    psItem.setDouble(5, 0.0);
                    psItem.executeUpdate();

                    // retry loop for transient SQLITE_BUSY
                    int attempts = 0;
                    int maxAttempts = 4;
                    while (true) {
                        try {
                            psUpdate.setInt(1, item.getQuantidade());
                            psUpdate.setInt(2, item.getProdutoId());
                            psUpdate.executeUpdate();
                            break;
                        } catch (SQLException ex) {
                            String msg = ex.getMessage() != null ? ex.getMessage() : "";
                            if (msg.contains("SQLITE_BUSY") && attempts < maxAttempts) {
                                attempts++;
                                try { Thread.sleep(100L * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                                continue;
                            }
                            throw ex;
                        }
                    }

                    // atualizar objeto em cache para publicar eventos depois
                    produto.setStockQty(produto.getStockQty() - item.getQuantidade());
                }
            }

            conn.commit();
            // publica evento com id da venda
            EventBus.getInstance().publish("sale.completed", vendaId);
            // publica eventos de alteração de estoque para cada produto
            for (Product p : produtosCache.values()) {
                EventBus.getInstance().publish("product.stockchanged", p);
                if (p.getStockQty() <= p.getMinStock()) EventBus.getInstance().publish("product.lowstock", p);
            }
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { /* ignora falha no rollback */ }
            throw e;
        } finally {
            try { conn.setAutoCommit(originalAutoCommit); } catch (Exception ignore) {}
        }
    }
}
