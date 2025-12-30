-- Arquivo de seeds extraído do schema.sql para aplicar manualmente se preferir
PRAGMA foreign_keys = ON;

INSERT OR IGNORE INTO categories (id, name, description) VALUES (1, 'Geral', 'Categoria padrão');
INSERT OR IGNORE INTO categories (id, name, description) VALUES (2, 'Alimentos', 'Produtos alimentícios');
INSERT OR IGNORE INTO categories (id, name, description) VALUES (3, 'Limpeza', 'Produtos de limpeza e higiene');

INSERT OR IGNORE INTO suppliers (id, name, contact, cnpj, email, address) VALUES (1, 'Fornecedor Exemplo', 'Contato', NULL, NULL, NULL);
INSERT OR IGNORE INTO suppliers (id, name, contact, cnpj, email, address) VALUES (2, 'Acme Distribuidor', '(11) 9999-0001', '12.345.678/0001-90', 'vendas@acme.com', 'Rua A, 123');
INSERT OR IGNORE INTO suppliers (id, name, contact, cnpj, email, address) VALUES (3, 'Mundo Limpo', '(11) 9999-0002', '98.765.432/0001-55', 'contato@mundolimpo.com', 'Av. B, 456');

INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address) VALUES (1, 'Cliente Teste', '00000000000', 'cliente@ex.com', NULL, NULL);
INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address) VALUES (2, 'João Silva', '11122233344', 'joao.silva@example.com', '(11) 91234-5678', 'Rua das Flores, 10');
INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address) VALUES (3, 'Maria Oliveira', '22233344455', 'maria.oliveira@example.com', '(21) 99876-5432', 'Av. Central, 200');
INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address) VALUES (4, 'Empresa XYZ', '12.345.678/0001-99', 'contato@xyz.com', '(31) 3333-4444', 'R. Industria, 500');

INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (1, 'COD-001','Produto Exemplo','Desc',1,1,10.0,20.0,100,5);
INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (2, 'ALM-001','Arroz 5kg','Arroz tipo 1',2,2,8.0,12.0,200,10);
INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (3, 'ALM-002','Feijão 1kg','Feijão carioca',2,2,4.0,7.0,150,8);
INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (4, 'LIM-001','Detergente 500ml','Detergente neutro',3,3,1.0,2.5,300,20);
INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (5, 'LIM-002','Desinfetante 1L','Desinfetante multiuso',3,3,2.5,5.0,120,10);

INSERT OR IGNORE INTO sales (id, sale_number, client_id, total_amount, paid, status) VALUES (1, 'SN1001', 2, 64.0, 1, 'COMPLETED');
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (1, 1, 2, 4, 12.0, 0.0);
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (2, 1, 4, 1, 2.5, 0.0);

INSERT OR IGNORE INTO logs (id, level, message, category, user_id) VALUES (1, 'INFO', 'Banco inicializado com dados de exemplo', 'init', NULL);

