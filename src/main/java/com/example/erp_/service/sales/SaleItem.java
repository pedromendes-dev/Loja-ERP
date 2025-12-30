package com.example.erp_.service.sales;

/**
 * Classe compatível antiga: SaleItem delega para ItemVenda.
 * @deprecated Use ItemVenda
 */
@Deprecated
public class SaleItem {
    private final ItemVenda delegate;

    public SaleItem() { this.delegate = new ItemVenda(); }
    public SaleItem(Integer productId, int quantity) { this.delegate = new ItemVenda(productId, quantity); }

    public Integer getProductId() { return delegate.getProdutoId(); }
    public void setProductId(Integer productId) { delegate.setProdutoId(productId); }
    public int getQuantity() { return delegate.getQuantidade(); }
    public void setQuantity(int quantity) { delegate.setQuantidade(quantity); }

    // métodos adicionais para acesso direto ao delegate, quando necessário
    public ItemVenda asItemVenda() { return delegate; }
}
