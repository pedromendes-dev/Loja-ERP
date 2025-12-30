package com.example.erp_.service.sales;

/**
 * Classe compatível antiga: SalesService delega para ServicoVendas.
 * @deprecated Use ServicoVendas
 */
@Deprecated
public class SalesService {
    private final ServicoVendas delegate = new ServicoVendas();

    public void performSale(java.util.List<SaleItem> items) throws Exception {
        // converte SaleItem (compat) para ItemVenda
        java.util.List<ItemVenda> itens = new java.util.ArrayList<>();
        if (items != null) {
            for (SaleItem s : items) {
                itens.add(new ItemVenda(s.getProductId(), s.getQuantity()));
            }
        }
        delegate.realizarVenda(itens);
    }
}
