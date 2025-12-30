# Estrutura recomendada do projeto

Esta documentação descreve a estrutura de pastas sugerida para o projeto "Loja" (ERP) e o propósito de cada diretório.

- src/main/java — código-fonte Java
  - com/example/erp — controllers, services, DAOs e modelos
  - com/example/ERP — utilitários, scripts de linha de comando e ferramentas internas
- src/main/resources — recursos (FXML, CSS, SQL)
  - fxml — telas JavaFX
  - css — estilos
  - db — schema.sql
- data — dados do projeto
  - exports — CSVs, PDFs e reports.zip
  - seeds.sql — dados para popular o DB
- scripts — scripts úteis (reorganizar, gerar reports)
- docs — documentação adicional
- target — pasta gerada pelo Maven (build)

Dicas
- Faça backup antes de executar scripts que movem arquivos.
- Revise `scripts/reorganize_project.ps1` antes de rodá-lo.
- Use `mvn -DskipTests=true package` para builds de apresentação.

