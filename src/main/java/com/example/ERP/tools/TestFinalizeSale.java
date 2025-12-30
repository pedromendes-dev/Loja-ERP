package com.example.ERP.tools;

import com.example.erp_.config.DbManager;
import com.example.erp_.model.Product;
import com.example.erp_.service.ProductService;
import com.example.erp_.service.sales.ItemVenda;
import com.example.erp_.service.sales.ServicoVendas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TestFinalizeSale {
    public static void main(String[] args) throws Exception {
        System.out.println("Initializing DB...");
        DbManager.getInstance().initialize();

        ProductService ps = new ProductService();
        List<Product> products = ps.findAll();
        if (products == null || products.isEmpty()) {
            System.out.println("No products found. Run seed or add products first.");
            return;
        }
        Product p = null;
        for (Product prod : products) {
            // stock is primitive int; just check > 0
            if (prod.getStockQty() > 0) { p = prod; break; }
        }
        if (p == null) {
            System.out.println("No product with stock > 0 found. Cannot finalize sale.");
            return;
        }

        System.out.println("Using product id=" + p.getId() + " name=" + p.getName() + " stock=" + p.getStockQty());

        ServicoVendas vendas = new ServicoVendas();
        List<ItemVenda> itens = new ArrayList<>();
        itens.add(new ItemVenda(p.getId(), 1));

        try {
            vendas.realizarVenda(itens);
            System.out.println("Venda realizada com sucesso.");
            try (Connection conn = DbManager.getInstance().getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id, sale_number, total_amount FROM sales ORDER BY id DESC LIMIT 1")) {
                if (rs.next()) {
                    System.out.println("Última venda: id=" + rs.getInt("id") + " num=" + rs.getString("sale_number") + " total=" + rs.getDouble("total_amount"));
                } else {
                    System.out.println("Nenhuma venda encontrada na tabela sales.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao finalizar venda: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
