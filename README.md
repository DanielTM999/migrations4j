# Documentação - Ferramenta de Migração de Bancos de Dados

## Visão Geral

Esta ferramenta é projetada para auxiliar na migração de dados entre dois bancos de dados distintos. Ela permite que você selecione as tabelas a serem migradas, execute a migração, gere scripts SQL e exporte os dados das tabelas para arquivos JSON.

## Sumário

- [Instalação](#instalação)
- [Configuração](#configuração)
- [Uso](#uso)
  - [Configuração de Conexão](#configuração-de-conexão)
  - [Migração de Dados](#migração-de-dados)
  - [Geração de Script SQL](#geração-de-script-sql)
  - [Exportação de Dados para JSON](#exportação-de-dados-para-json)
  - [Log de Migração](#log-de-migração)
- [Exemplos](#exemplos)
- [Interfaces e Classes](#interfaces-e-classes)
- [Considerações Finais](#considerações-finais)

## Instalação

Adicione a biblioteca `migrations4j` ao seu projeto. Certifique-se de incluir as dependências necessárias para conexão com o banco de dados, como drivers JDBC.

## Configuração

Para utilizar a ferramenta, é necessário configurar as conexões com o banco de dados de origem e o banco de dados de destino.

### Conexões

As conexões são configuradas utilizando a classe `SourceConnectionDetails`, que armazena as informações de conexão (URL, usuário, senha e driver).

### Configuração Exemplo

```java
SourceConnectionDetails connectionDetailsHost = new SourceConnectionDetails(
    "jdbc:mysql://localhost:3306/origem", 
    "user", 
    "password", 
    "com.mysql.cj.jdbc.Driver"
);

SourceConnectionDetails connectionDetailsTarget = new SourceConnectionDetails(
    "jdbc:mysql://localhost:3306/destino", 
    "user", 
    "password", 
    "com.mysql.cj.jdbc.Driver"
);

DataBaseActionsService dbService = new DataBaseActionsService(connectionDetailsHost, connectionDetailsTarget);
```

## Uso

### Configuração de Conexão

Você pode configurar manualmente as conexões utilizando os métodos `setDbHost` e `setDbTarget`.

```java
dbService.setDbHost(connectionDetailsHost);
dbService.setDbTarget(connectionDetailsTarget);
```

### Migração de Dados

Para migrar dados entre os bancos de dados, utilize o `Migrator`. Primeiro, configure as tabelas a serem migradas e, em seguida, execute a migração.

```java
Migrator migrator = dbService.getMigrator();
migrator.setTablesToMigrate("tabela1", "tabela2");
migrator.executeMigration();
```

### Geração de Script SQL

A ferramenta permite gerar scripts SQL com os dados a serem inseridos no banco de dados de destino.

```java
String script = migrator.createScriptMigration();
File scriptFile = migrator.createScriptMigrationFile("caminho/para/arquivo.sql");
```

### Exportação de Dados para JSON

Você também pode exportar os dados das tabelas do banco de dados de origem para arquivos JSON.

```java
String outputDir = "caminho/para/exportação";
String jsonPaths = dbService.exportDataToJson(outputDir, Arrays.asList("tabela1", "tabela2"));
```

### Log de Migração

Durante o processo de migração, a ferramenta gera um log detalhado que registra todas as etapas e possíveis erros que possam ocorrer. Esse log é útil para auditoria e depuração.

#### Acessando o Log

Você pode acessar o log da migração chamando o método `getMigratorLog()` e, em seguida, usando o método `writeLog()` para obter o conteúdo completo do log.

```java
MigratorLog log = migrator.getMigratorLog();
System.out.println(log.writeLog());
```

### Conteúdo do Log

O log inclui as seguintes informações:

- **Início e conclusão da migração para cada tabela:** Informa quando a migração de uma tabela específica começou e terminou.
- **Consultas preparadas para inserção:** Mostra as consultas SQL que foram geradas para inserir os dados.
- **Valores das linhas inseridas:** Detalha os valores de cada linha que foi migrada.
- **Erros:** Registra quaisquer erros ocorridos durante a migração, incluindo o estado SQL, código de erro e mensagem.
- **Rollback de transações:** Informa se ocorreu algum rollback devido a erros durante o processo de migração.
- **Confirmação de migração:** Informa se a migração foi confirmada com sucesso ou se houve algum problema.

### Exemplos

#### Migração Completa com Exportação para JSON

```java
Migrator migrator = dbService.getMigrator();
migrator.setTablesToMigrate("tabela1", "tabela2");
migrator.executeMigration();

String outputDir = "caminho/para/exportação";
String jsonPaths = dbService.exportDataToJson(outputDir, Arrays.asList("tabela1", "tabela2"));

MigratorLog log = migrator.getMigratorLog();
System.out.println(log.writeLog());
```

## Interfaces e Classes

- **DataBaseActions:** Interface para ações de banco de dados, como exportação de dados para JSON e obtenção do Migrator.
- **Migrator:** Interface para realizar a migração de tabelas entre bancos de dados.
- **MigratorService:** Implementação do Migrator, responsável por executar a migração e gerar scripts SQL.
- **MigratorLog:** Interface para criação de logs durante o processo de migração.
- **MigratorLogServiceConsole:** Implementação do MigratorLog que gera logs no console.
- **SourceConnectionDetails:** Classe que contém os detalhes de conexão com o banco de dados.

## Considerações Finais

Essa ferramenta é útil para realizar migrações controladas e exportações de dados, permitindo manter registros detalhados de cada etapa do processo. Use-a para garantir que suas migrações de banco de dados sejam realizadas de maneira segura e documentada.
