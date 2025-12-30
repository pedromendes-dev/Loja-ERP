@echo off
REM Script para gerar exports localmente
cd /d "%~dp0.."
REM 1) Compilar
mvn -DskipTests=true clean package
REM 2) Copiar dependências
mvn -DskipTests=true dependency:copy-dependencies -DoutputDirectory=target/dependency
REM 3) Executar utilitário ExportCsvPrinter
java -Duser.home="%cd%" -cp "target/ERP-1.0-SNAPSHOT.jar;target/dependency/*" com.example.ERP.tools.ExportCsvPrinter
pause

