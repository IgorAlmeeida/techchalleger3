# Agendamento — Sistema de Agendamento e Gerenciamento para Serviços de Beleza e Bem-Estar

Projeto da Fase 3 (Substitutiva) da Pós Tech FIAP — Arquitetura e Desenvolvimento Java. Sistema de agendamento para estabelecimentos de beleza e bem-estar, construído em Java/Spring Boot seguindo Clean Architecture.

## Visão geral

O sistema permite que estabelecimentos cadastrem serviços e profissionais, clientes agendem horários online, avaliem os atendimentos, e o estabelecimento gerencie sua agenda (cancelamentos, confirmação de presença). Cobre as 7 funcionalidades do enunciado do Tech Challenge: cadastro de estabelecimentos, perfil de profissionais, agendamento de serviços com notificação automática, avaliações, busca/filtragem, gerenciamento de agendamentos e exportação para calendário externo.

## Arquitetura

Clean Architecture em 4 camadas, com a regra de dependência sempre apontando para dentro:

```
br.com.fiap.techchalleger3.agendamento
├── domain/          → entidades e regras de negócio puras, sem dependência de framework
├── application/      → casos de uso (usecase) e portas (port) — as interfaces que a infraestrutura implementa
├── infrastructure/   → adapters concretos: persistência (JPA), autenticação (Keycloak), e-mail, agendador
└── interfaces/rest/  → controllers, DTOs e assemblers da API REST
```

Pontos que valem menção:
- **Ports & Adapters:** use cases dependem só de interfaces (`EmailSenderPort`, repositórios, etc.), nunca de implementação concreta — troca de tecnologia de infraestrutura não exige tocar em regra de negócio.
- **Infraestrutura enxuta de propósito:** o projeto rodou com RabbitMQ e Redis em algum momento e ambos foram removidos deliberadamente (ver `02-Specs/06` e `07` na documentação) — o enunciado não exige mensageria nem cache, e a complexidade não se justificava para o volume do sistema. Notificação de e-mail hoje é assíncrona in-process (`@Async` do Spring), sem broker externo.

## Stack

- Java 21, Spring Boot 3.5.3, Maven
- PostgreSQL + Liquibase (migrations)
- Keycloak (autenticação/autorização via OAuth2 Resource Server)
- JUnit 5 + Mockito, Testcontainers (Postgres/Keycloak reais em teste), Cucumber (BDD), JaCoCo (cobertura, gate de 80% em `domain`/`application`)
- SonarCloud (análise estática)
- springdoc-openapi (Swagger UI)
- GitHub Actions (CI/CD) + GHCR (registro de imagem Docker)

## Rodando localmente

Pré-requisitos: Docker, Java 21, Maven (ou use o wrapper `./mvnw`).

```bash
# Sobe Postgres + Keycloak
docker compose up -d

# Roda a aplicação
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Keycloak fica em `http://localhost:8180` (realm `agendamento`, importado automaticamente de `docker/keycloak/import`).

### Variáveis de ambiente necessárias

| Variável | Descrição |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Credenciais do Postgres local (default já definido no `compose.yaml`) |
| `GMAIL_USERNAME`, `GMAIL_APP_PASSWORD` | Conta usada para envio de e-mails de confirmação/notificação (senha de app do Gmail, não a senha normal da conta) |
| `KEYCLOAK_CLIENT_SECRET` | Secret do client `agendamento-client` configurado no realm |

Pode-se usar um arquivo `.env` na raiz (o projeto usa `spring-dotenv` para carregá-lo automaticamente).

## Documentação da API

Com a aplicação rodando: **Swagger UI** em `http://localhost:8080/swagger-ui.html`.

## Testes

```bash
# Roda toda a suíte (unitários + integração via Testcontainers + BDD/Cucumber) e o gate de cobertura
./mvnw verify

# Relatório de cobertura (JaCoCo)
open target/site/jacoco/index.html

# Relatório dos cenários Cucumber
open target/cucumber-reports/index.html
```

A suíte de integração sobe Postgres, Keycloak (via Testcontainers) automaticamente — não depende de nenhum serviço externo já rodando.

## CI/CD

O pipeline (`.github/workflows/build.yml`) roda em 4 fases sequenciais a cada push/PR na `main`:

1. **Build** — compila, falha rápido em erro de compilação.
2. **Testes + Cobertura** — suíte completa (unitário/integração/BDD) + gate de cobertura JaCoCo.
3. **SonarCloud** — reaproveita os artefatos de cobertura do job anterior (não roda os testes de novo).
4. **Publicação no GHCR** — build e push da imagem Docker (`ghcr.io/igoralmeeida/techchallenger3`), só em push na `main` e só se os jobs anteriores passarem.

## Deploy

- **Local:** `docker compose up -d` + `./mvnw spring-boot:run` (ou build da imagem via `Dockerfile` na raiz).
- **Azure (Container Apps):** infraestrutura como código em `infra/azure/` (Bicep) — ver `infra/azure/README.md` para o passo a passo.

## Estrutura de documentação/specs

Decisões de arquitetura e planejamento são documentadas como specs numeradas antes da implementação (vault Obsidian do projeto, fora deste repositório): cobrem desde o desenho inicial da Clean Architecture até decisões pontuais como a remoção do RabbitMQ/Redis e a estrutura de deploy no Azure.

## Licença

Projeto acadêmico — Pós Tech FIAP, Fase 3 (Substitutiva).
