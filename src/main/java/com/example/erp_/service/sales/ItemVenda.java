package com.example.erp_.service.sales;

/**
 * Item da venda: associa um produto (por id) e a quantidade vendida.
 */
public class ItemVenda {
    private Integer produtoId;
    private int quantidade;

    public ItemVenda() {}

    public ItemVenda(Integer produtoId, int quantidade) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public Integer getProdutoId() { return produtoId; }
    public void setProdutoId(Integer produtoId) { this.produtoId = produtoId; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}

