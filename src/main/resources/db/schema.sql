-- Esquema para Loja
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'USER',
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    cpf_cnpj TEXT UNIQUE,
    email TEXT,
    phone TEXT,
    address TEXT,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT
);

CREATE TABLE IF NOT EXISTS suppliers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    contact TEXT,
    cnpj TEXT,
    email TEXT,
    address TEXT,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sku TEXT UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
    supplier_id INTEGER REFERENCES suppliers(id) ON DELETE SET NULL,
    cost_price REAL NOT NULL DEFAULT 0,
    sale_price REAL NOT NULL DEFAULT 0,
    stock_qty INTEGER NOT NULL DEFAULT 0,
    min_stock INTEGER NOT NULL DEFAULT 0,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT
);

CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);

CREATE TABLE IF NOT EXISTS sales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_number TEXT UNIQUE,
    date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_id INTEGER REFERENCES clients(id) ON DELETE SET NULL,
    total_amount REAL NOT NULL,
    paid INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'COMPLETED'
);

CREATE TABLE IF NOT EXISTS sale_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id INTEGER NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price REAL NOT NULL,
    discount REAL NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    level TEXT NOT NULL,
    message TEXT NOT NULL,
    category TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id INTEGER REFERENCES users(id)
);

-- Dados seed básicos (o admin será criado pelo AuthService se não existir)
INSERT OR IGNORE INTO categories (id, name, description) VALUES (1, 'Geral', 'Categoria padrão');
INSERT OR IGNORE INTO suppliers (id, name, contact) VALUES (1, 'Fornecedor Exemplo', 'Contato');
INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email) VALUES (1, 'Cliente Teste', '00000000000', 'cliente@ex.com');
INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (1, 'COD-001','Produto Exemplo','Desc',1,1,10.0,20.0,100,5);

-- Mais dados de exemplo para facilitar testes e demonstrações
INSERT OR IGNORE INTO categories (id, name, description) VALUES (2, 'Alimentos', 'Produtos alimentícios');
INSERT OR IGNORE INTO categories (id, name, description) VALUES (3, 'Limpeza', 'Produtos de limpeza e higiene');

INSERT OR IGNORE INTO suppliers (id, name, contact, cnpj, email, address) VALUES (2, 'Acme Distribuidor', '(11) 9999-0001', '12.345.678/0001-90', 'vendas@acme.com', 'Rua A, 123');
INSERT OR IGNORE INTO suppliers (id, name, contact, cnpj, email, address) VALUES (3, 'Mundo Limpo', '(11) 9999-0002', '98.765.432/0001-55', 'contato@mundolimpo.com', 'Av. B, 456');

INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address) VALUES (2, 'João Silva', '11122233344', 'joao.silva@example.com', '(11) 91234-5678', 'Rua das Flores, 10');
INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address) VALUES (3, 'Maria Oliveira', '22233344455', 'maria.oliveira@example.com', '(21) 99876-5432', 'Av. Central, 200');
INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address) VALUES (4, 'Empresa XYZ', '12.345.678/0001-99', 'contato@xyz.com', '(31) 3333-4444', 'R. Industria, 500');

-- NOVOS CLIENTES ADICIONADOS PELO USUÁRIO
INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address, active) VALUES (10, 'Rafael Costa', '33344455566', 'rafael.costa@example.com', '(11) 91234-0001', 'Rua Nova 10', 1);
INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address, active) VALUES (11, 'Ana Paula', '44455566677', 'ana.paula@example.com', '(21) 99888-7777', 'Av. Central 200', 1);
INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address, active) VALUES (12, 'Empresa ABC', '99.888.777/0001-66', 'contato@empresaabc.com', '(31) 3333-9999', 'R. Industrial 500', 1);

INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (2, 'ALM-001','Arroz 5kg','Arroz tipo 1',2,2,8.0,12.0,200,10);
INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (3, 'ALM-002','Feijão 1kg','Feijão carioca',2,2,4.0,7.0,150,8);
INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (4, 'LIM-001','Detergente 500ml','Detergente neutro',3,3,1.0,2.5,300,20);
INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock) VALUES (5, 'LIM-002','Desinfetante 1L','Desinfetante multiuso',3,3,2.5,5.0,120,10);

-- Exemplo de vendas
INSERT OR IGNORE INTO sales (id, sale_number, client_id, total_amount, paid, status) VALUES (1, 'SN1001', 2, 64.0, 1, 'COMPLETED');
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (1, 1, 2, 4, 12.0, 0.0);
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (2, 1, 4, 1, 2.5, 0.0);

-- Mais vendas de exemplo
INSERT OR IGNORE INTO sales (id, sale_number, client_id, total_amount, paid, status) VALUES (2, 'SN1002', 3, 31.0, 1, 'COMPLETED');
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (3, 2, 3, 2, 7.0, 0.0);
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (4, 2, 4, 3, 2.5, 0.0);

INSERT OR IGNORE INTO sales (id, sale_number, client_id, total_amount, paid, status) VALUES (3, 'SN1003', 4, 120.0, 0, 'PENDING');
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (5, 3, 1, 6, 20.0, 0.0);

INSERT OR IGNORE INTO sales (id, sale_number, client_id, total_amount, paid, status) VALUES (4, 'SN1004', 10, 40.0, 1, 'COMPLETED');
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (6, 4, 2, 2, 12.0, 0.0);
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (7, 4, 3, 1, 7.0, 0.0);

INSERT OR IGNORE INTO sales (id, sale_number, client_id, total_amount, paid, status) VALUES (5, 'SN1005', 11, 15.0, 1, 'COMPLETED');
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (8, 5, 4, 6, 2.5, 0.0);

INSERT OR IGNORE INTO sales (id, sale_number, client_id, total_amount, paid, status) VALUES (6, 'SN1006', 12, 54.0, 0, 'PENDING');
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (9, 6, 2, 3, 12.0, 0.0);
INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (10, 6, 5, 6, 5.0, 0.0);

-- Logs de exemplo
INSERT OR IGNORE INTO logs (id, level, message, category, user_id) VALUES (1, 'INFO', 'Banco inicializado com dados de exemplo', 'init', NULL);

-- Total final de seeds inseridos: categories(3), suppliers(3), clients(4), products(5), sales(6)
