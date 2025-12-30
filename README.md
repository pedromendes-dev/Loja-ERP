# Loja (ERP) — Projeto organizado

Este repositório contém o sistema "Loja" (ERP) em Java/JavaFX. Eu reorganizei a documentação e adicionei scripts para facilitar a apresentação e a manutenção do projeto.

Sumário rápido
- Como compilar e rodar
- Como gerar relatórios (CSV/PDF/ZIP)
- Como adicionar clientes via CSV
- Estrutura proposta do projeto
- Script para reorganizar arquivos localmente

Requisitos
- Java 17+
- Maven 3.6+
- (Opcional) sqlite3 para manipular banco local

Como compilar e rodar a aplicação

1. Compilar (gera `target/`):

```powershell
Set-Location 'C:\Users\IgrejaIPB\Downloads\Project\PROJETO-A3-UNA'
mvn -DskipTests=true clean package
```

2. Rodar a aplicação JavaFX:

```powershell
mvn javafx:run
```

Gerar relatórios (CSV, PDF e ZIP)

- Já existem utilitários Java no pacote `com.example.ERP.tools`:
  - `ExportCsvPrinter` — exporta tabelas (clients, products, sales) para `data/exports/`.
  - `ClientsPdfReport`, `ProductsPdfReport`, `SalesPdfReport` — geram PDFs em `data/exports/`.
  - `ReportsPackager` — roda os geradores e empacota `data/exports/` em `data/exports/reports.zip`.

Para gerar os relatórios localmente (exemplo PowerShell):

```powershell
# compilar e copiar dependências (apenas primeira vez)
mvn -DskipTests=true compile
mvn -DskipTests=true dependency:copy-dependencies -DoutputDirectory=target/dependency

# gerar relatórios (usa arquivos CSV em data/exports/)
java -cp "target/classes;target/dependency/*" com.example.ERP.tools.ReportsPackager
```

Inserir clientes via CSV

- Arquivo exemplo: `data/new_clients.csv` (colunas: name,cpf_cnpj,email,phone,address,active)
- Utilitários:
  - `AddClients` — usa `DbManager` para inicializar o DB e inserir registros.
  - `AddClientsDirect` — insere diretamente num arquivo SQLite se quiser especificar o caminho do DB.

Exemplo (inserir e depois gerar exports):

```powershell
# usa user.home apontando para a pasta do projeto para forçar DB local em ./.minierp
java -Duser.home="%cd%" -cp "target/classes;target/dependency/*" com.example.ERP.tools.AddClients
java -Duser.home="%cd%" -cp "target/classes;target/dependency/*" com.example.ERP.tools.ReportsPackager
```

Reorganizar pastas localmente

- Existe um script PowerShell em `scripts/reorganize_project.ps1` que cria a árvore de pastas recomendada e move arquivos comuns para lá. Leia o script antes de executar e faça um commit/backup.

Estrutura proposta (resumo)

```
PROJETO-A3-UNA/
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  ├─ com/example/erp/         # código principal, controllers
│  │  │  └─ com/example/ERP/         # utilitários/tools
│  │  └─ resources/
│  │     ├─ fxml/                   # views FXML
│  │     ├─ css/                    # estilos
│  │     └─ db/                     # schema.sql
├─ data/
│  ├─ exports/                      # CSVs, PDFs, reports.zip
│  └─ seeds.sql
├─ scripts/                         # scripts úteis (reorganizar, gerar)
├─ docs/                            # documentação (STRUCTURE.md)
└─ README.md
```

Notas finais
- Revise `scripts/reorganize_project.ps1` antes de executá-lo — ele apenas move arquivos conhecidos para a estrutura proposta.

