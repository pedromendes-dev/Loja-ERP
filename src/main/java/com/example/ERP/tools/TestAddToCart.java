package com.example.ERP.tools;

public class TestAddToCart {
    public static void main(String[] args) throws Exception {
        System.out.println("Initializing DB manager...");
        com.example.erp_.config.DbManager.getInstance().initialize();

        System.out.println("Loading products...");
        com.example.erp_.service.ProductService ps = new com.example.erp_.service.ProductService();
        java.util.List<com.example.erp_.model.Product> products = ps.findAll();
        if (products == null || products.isEmpty()) {
            System.out.println("No products found in DB. Seed the DB first.");
            return;
        }
        com.example.erp_.model.Product p = products.get(0);
        System.out.println("Found product: id=" + p.getId() + " name=" + p.getName() + " stock=" + p.getStockQty());

        com.example.erp_.service.sales.ServicoCarrinho cart = new com.example.erp_.service.sales.ServicoCarrinho(new com.example.erp_.service.ProductService());
        try {
            cart.adicionar(p.getId(), 1);
            System.out.println("Added product to cart. Items count=" + cart.getItens().size());
            cart.getItens().forEach(it -> System.out.println("- " + it.getProdutoId() + " x" + it.getQuantidade() + " -> " + it.getNome()));
        } catch (Exception e) {
            System.out.println("Error adding to cart: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}

