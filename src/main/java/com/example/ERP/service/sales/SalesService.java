package com.example.erp.service.sales;

import com.example.erp_.config.DbManager;
import com.example.erp_.model.Product;
import com.example.erp_.patterns.observer.EventBus;
import com.example.erp_.service.ProductService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Serviço de vendas: realiza uma venda persistindo registro de venda e itens, e decrementando o estoque.
 * A operação completa é executada dentro de uma transação JDBC: se qualquer etapa falhar, as alterações no banco são revertidas.
 */
public class SalesService {
    private final ProductService productService = new ProductService();

    /**
     * Executa a venda de forma transacional: persiste venda + itens e decrementa estoque.
     */
    public void performSale(List<SaleItem> items) throws Exception {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Itens da venda não informados");

        Connection conn = DbManager.getInstance().getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // valida disponibilidade e calcula total
            double total = 0.0;
            for (SaleItem it : items) {
                Product p = productService.findById(it.getProductId());
                if (p == null) throw new IllegalArgumentException("Produto não encontrado: " + it.getProductId());
                if (p.getStockQty() < it.getQuantity()) throw new IllegalArgumentException("Quantidade insuficiente para produto: " + p.getName());
                total += p.getSalePrice() * it.getQuantity();
            }

            // insere venda
            String saleNumber = "SN" + System.currentTimeMillis();
            String insertSaleSql = "INSERT INTO sales (sale_number, total_amount, paid, status) VALUES (?, ?, ?, ?)";
            int saleId;
            try (PreparedStatement ps = conn.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, saleNumber);
                ps.setDouble(2, total);
                ps.setInt(3, 1); // pago
                ps.setString(4, "COMPLETED");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) saleId = rs.getInt(1); else throw new IllegalStateException("Erro ao recuperar id da venda");
                }
            }

            // insere itens da venda e decremente o estoque
            String insertItemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, discount) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                for (SaleItem it : items) {
                    Product p = productService.findById(it.getProductId());
                    psItem.setInt(1, saleId);
                    psItem.setInt(2, it.getProductId());
                    psItem.setInt(3, it.getQuantity());
                    psItem.setDouble(4, p.getSalePrice());
                    psItem.setDouble(5, 0.0);
                    psItem.executeUpdate();

                    // decrementa estoque usando ProductService que usa o mesmo gerenciador de conexões
                    productService.adjustStock(it.getProductId(), -it.getQuantity());
                }
            }

            conn.commit();
            // publica evento com o id da venda
            EventBus.getInstance().publish("sale.completed", saleId);
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { /* ignora falha no rollback */ }
            throw e;
        } finally {
            try { conn.setAutoCommit(originalAutoCommit); } catch (Exception ignore) {}
        }
    }
}
