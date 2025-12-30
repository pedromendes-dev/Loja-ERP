# Script que reorganiza o repositório em uma estrutura padrão
# Leia antes de executar. Este script moverá arquivos e criará pastas.

$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $root

Write-Host "Criando pastas..."
New-Item -ItemType Directory -Force -Path src\main\java\com\example\erp | Out-Null
New-Item -ItemType Directory -Force -Path src\main\java\com\example\ERP | Out-Null
New-Item -ItemType Directory -Force -Path src\main\resources\fxml | Out-Null
New-Item -ItemType Directory -Force -Path src\main\resources\css | Out-Null
New-Item -ItemType Directory -Force -Path src\main\resources\db | Out-Null
New-Item -ItemType Directory -Force -Path data\exports | Out-Null
New-Item -ItemType Directory -Force -Path scripts | Out-Null
New-Item -ItemType Directory -Force -Path docs | Out-Null

Write-Host "Movendo arquivos de recursos (exemplos)..."
Get-ChildItem -Path . -Include *.fxml -Recurse -ErrorAction SilentlyContinue | ForEach-Object {
    Move-Item -Path $_.FullName -Destination src\main\resources\fxml -Force
}
Get-ChildItem -Path . -Include *.css -Recurse -ErrorAction SilentlyContinue | ForEach-Object {
    Move-Item -Path $_.FullName -Destination src\main\resources\css -Force
}
Get-ChildItem -Path . -Include schema.sql -Recurse -ErrorAction SilentlyContinue | ForEach-Object {
    Move-Item -Path $_.FullName -Destination src\main\resources\db -Force
}

Write-Host "Movendo scripts e docs..."
Get-ChildItem -Path . -Include *.bat,*.ps1 -Recurse -ErrorAction SilentlyContinue | ForEach-Object {
    Move-Item -Path $_.FullName -Destination scripts -Force
}

Write-Host "Movendo exports para data/exports..."
Get-ChildItem -Path . -Include *.csv,*.pdf,*.zip -Recurse -ErrorAction SilentlyContinue | ForEach-Object {
    Move-Item -Path $_.FullName -Destination data\exports -Force
}

Write-Host "Criando doc de estrutura..."
@"
Estrutura proposta:

PROJETO-A3-UNA/
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  ├─ com/example/erp/
│  │  │  └─ com/example/ERP/
│  │  └─ resources/
│  │     ├─ fxml/
│  │     ├─ css/
│  │     └─ db/
├─ data/
│  └─ exports/
├─ scripts/
├─ docs/
"@ | Out-File docs\STRUCTURE.md -Encoding UTF8

Write-Host "Reorganização concluída. Revise as alterações e faça commit manualmente."
