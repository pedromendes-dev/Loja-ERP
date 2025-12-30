package com.example.erp_.service.sales;

public class CarrinhoItem {
    private Integer produtoId;
    private String nome;
    private int quantidade;
    private double precoUnitario;

    public CarrinhoItem(Integer produtoId, String nome, int quantidade, double precoUnitario) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public Integer getProdutoId() { return produtoId; }
    public String getNome() { return nome; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public double getSubtotal() { return precoUnitario * quantidade; }
}

