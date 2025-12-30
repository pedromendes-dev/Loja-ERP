package com.example.erp_.service.sales;

import com.example.erp_.model.Product;
import com.example.erp_.service.ProductService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServicoCarrinho {
    private final ProductService productService;
    private final List<CarrinhoItem> itens = new ArrayList<>();

    public ServicoCarrinho(ProductService productService) {
        this.productService = productService;
    }

    public List<CarrinhoItem> getItens() { return Collections.unmodifiableList(itens); }

    public void limpar() { itens.clear(); }

    public double getTotal() {
        return itens.stream().mapToDouble(CarrinhoItem::getSubtotal).sum();
    }

    public void adicionar(Integer produtoId, int quantidade) throws Exception {
        if (quantidade < 1) throw new IllegalArgumentException("Quantidade deve ser >=1");
        Product p = productService.findById(produtoId);
        if (p == null) throw new IllegalArgumentException("Produto não encontrado");
        int somado = quantidade;
        CarrinhoItem existente = null;
        for (CarrinhoItem it : itens) if (it.getProdutoId().equals(produtoId)) { existente = it; somado += it.getQuantidade(); }
        if (somado > p.getStockQty()) throw new IllegalStateException("Estoque insuficiente");
        if (existente != null) existente.setQuantidade(existente.getQuantidade() + quantidade);
        else itens.add(new CarrinhoItem(produtoId, p.getName(), quantidade, p.getSalePrice()));
    }

    public void atualizarQuantidade(Integer produtoId, int novaQuantidade) throws Exception {
        if (novaQuantidade < 1) throw new IllegalArgumentException("Quantidade deve ser >=1");
        Product p = productService.findById(produtoId);
        if (p == null) throw new IllegalArgumentException("Produto não encontrado");
        if (novaQuantidade > p.getStockQty()) throw new IllegalStateException("Estoque insuficiente");
        for (CarrinhoItem it : itens) if (it.getProdutoId().equals(produtoId)) { it.setQuantidade(novaQuantidade); return; }
        throw new IllegalArgumentException("Item não está no carrinho");
    }

    public void remover(Integer produtoId) {
        itens.removeIf(i -> i.getProdutoId().equals(produtoId));
    }
}

