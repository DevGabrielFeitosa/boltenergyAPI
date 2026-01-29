# Process File API

API RESTful para processamento e consulta de dados de usinas geradoras de energia do Brasil (ANEEL).

## Tecnologias

- Kotlin
- Spring Boot 4.0.2
- PostgreSQL
- Gradle

## Funcionalidades

- Download automático de arquivo CSV da ANEEL (agendado)
- Processamento e normalização de dados em lote
- API REST para consulta dos maiores geradores do Brasil

## Configuração

### Banco de Dados

```yaml
PostgreSQL
Host: localhost
Port: 5433
Database: boltenergy
User: postgres
Password: postgres
```

### Executar

```bash
./gradlew bootRun
```

## Endpoints

### GET /api/v1/power-plants/top-generators

Retorna os 5 maiores geradores de energia do Brasil ordenados por potência outorgada (kW).

**Parâmetros:**
- `limit` (opcional, padrão: 5) - Quantidade de resultados

**Exemplo:**
```
GET http://localhost:8080/api/v1/power-plants/top-generators?limit=10
```

**Response:**
```json
{
  "titulo": "Top 5 Maiores Geradores de Energia do Brasil",
  "totalRegistros": 5,
  "geradores": [
    {
      "ranking": 1,
      "id": 12345,
      "nomEmpreendimento": "UHE Itaipu",
      "mdaPotenciaOutorgadaKw": 14000000.0,
      "sigUFPrincipal": "PR",
      "sigTipoGeracao": "UHE",
      "dscOrigemCombustivel": "Água",
      ...
    }
  ]
}
```

## Estrutura do Projeto

```
src/main/kotlin/br/com/boltenergy/process_file_api/
├── controller/       # Endpoints REST
├── service/          # Lógica de negócio
├── repository/       # Acesso ao banco de dados
├── entity/           # Entidades JPA
└── dto/              # Objetos de transferência
```

## Build

```bash
./gradlew build
```
