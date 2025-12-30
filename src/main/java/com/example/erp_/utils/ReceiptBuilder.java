package com.example.erp_.utils;

import com.example.erp_.model.Product;
import com.example.erp_.service.ProductService;
import com.example.erp_.service.sales.ItemVenda;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReceiptBuilder {
    public static List<String> buildReceiptLines(Integer saleId, List<ItemVenda> itens, ProductService productService) {
        List<String> lines = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lines.add("Venda #: " + saleId);
        lines.add("Data: " + LocalDateTime.now().format(df));
        lines.add("-----------------------------");
        double total = 0.0;
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt","BR"));
        for (ItemVenda it : itens) {
            try {
                Product p = productService.findById(it.getProdutoId());
                double subtotal = p.getSalePrice() * it.getQuantidade();
                lines.add(p.getName() + " x" + it.getQuantidade() + " = " + nf.format(subtotal));
                total += subtotal;
            } catch (Exception e) {
                lines.add("Produto #" + it.getProdutoId() + " x" + it.getQuantidade());
            }
        }
        lines.add("");
        lines.add("Total: " + NumberFormat.getCurrencyInstance(new Locale("pt","BR")).format(total));
        lines.add("-----------------------------");
        lines.add("Obrigado pela compra!");
        return lines;
    }
}

