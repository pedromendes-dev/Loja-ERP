package com.example.erp_.controller;

/**
 * Shim: delega para `com.example.erp.controller.FormularioClienteController`
 */
public class FormularioClienteController extends com.example.erp.controller.FormularioClienteController {

    // Sobrecarga conveniente que aceita o modelo underscore e delega para o método pai
    public void setClient(com.example.erp_.model.Client c) {
        // com.example.erp_.model.Client estende com.example.erp.model.Client, portanto pode ser passado ao super
        super.setClient(c);
    }

    // Override covariante para retornar o tipo underscore esperado pelo código legacy
    @Override
    public com.example.erp_.model.Client getClient() {
        com.example.erp.model.Client c = super.getClient();
        if (c == null) return null;
        if (c instanceof com.example.erp_.model.Client) return (com.example.erp_.model.Client) c;
        // Constrói uma instância underscore copiando campos básicos
        com.example.erp_.model.Client out = new com.example.erp_.model.Client();
        out.setId(c.getId());
        out.setName(c.getName());
        out.setCpfCnpj(c.getCpfCnpj());
        out.setEmail(c.getEmail());
        out.setPhone(c.getPhone());
        out.setAddress(c.getAddress());
        out.setActive(c.isActive());
        out.setCreatedAt(c.getCreatedAt());
        out.setUpdatedAt(c.getUpdatedAt());
        return out;
    }
}
