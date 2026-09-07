# Graph Report - C:\code\personal\AgendaFacil\src\main\java  (2026-09-06)

## Corpus Check
- Corpus is ~27,822 words - fits in a single context window. You may not need a graph.

## Summary
- 1126 nodes · 5181 edges · 33 communities
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 538 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- REST Controllers & Security
- Repository Ports (Agenda/Cache)
- Agendamento Query Ports
- Domain Enums
- Profissional & Servico Ports
- Escala & Agenda Save Ports
- Redis Cache Infrastructure
- Email & FilaEspera (Removed)
- Agendamento Status Domain
- Core Domain Models
- UnidadeSaude to Estabelecimento
- Agenda Item Mappers
- Mixed Application Ports
- Cliente Registration
- Auth & Keycloak
- Persistence Converters
- Empresa Domain
- Use Case Layer
- Escala Domain
- Spring Config & OpenAPI
- Scheduler Jobs
- Keycloak Admin Adapter
- Error Handling
- Agendamento Use Cases
- Domain Services
- REST DTOs
- FilaEspera Use Cases (Removed)
- Liquibase Migrations
- RabbitMQ Config
- Prestador Use Cases
- Vinculos Use Cases
- Email Templates
- Senha & Token Utils

## God Nodes (most connected - your core abstractions)
1. `RegistroNaoEncontradoException` - 83 edges
2. `Agendamento` - 71 edges
3. `PrestadorVinculoRepositoryPort` - 69 edges
4. `PrestadorRepositoryPort` - 63 edges
5. `StatusAgendamentoEnum` - 56 edges
6. `Prestador` - 51 edges
7. `PrestadorVinculo` - 50 edges
8. `UsuarioRepositoryPort` - 48 edges
9. `FilaEspera` - 48 edges
10. `UnidadeSaude` - 45 edges

## Surprising Connections (you probably didn't know these)
- `AgendaItemRepositoryPortImpl` --implements--> `AgendaItemRepositoryPort`  [EXTRACTED]
  br/com/fiap/techchalleger/agendafacil/infrastructure/persistence/repository/AgendaItemRepositoryPortImpl.java → br/com/fiap/techchalleger/agendafacil/application/port/AgendaItemRepositoryPort.java
- `AgendaRepositoryPortImpl` --implements--> `AgendaRepositoryPort`  [EXTRACTED]
  br/com/fiap/techchalleger/agendafacil/infrastructure/persistence/repository/AgendaRepositoryPortImpl.java → br/com/fiap/techchalleger/agendafacil/application/port/AgendaRepositoryPort.java
- `AgendamentoRepositoryPortImpl` --implements--> `AgendamentoRepositoryPort`  [EXTRACTED]
  br/com/fiap/techchalleger/agendafacil/infrastructure/persistence/repository/AgendamentoRepositoryPortImpl.java → br/com/fiap/techchalleger/agendafacil/application/port/AgendamentoRepositoryPort.java
- `FecharAgendamentosJob` --references--> `AgendamentoRepositoryPort`  [EXTRACTED]
  br/com/fiap/techchalleger/agendafacil/infrastructure/scheduler/FecharAgendamentosJob.java → br/com/fiap/techchalleger/agendafacil/application/port/AgendamentoRepositoryPort.java
- `RedisCachePortImpl` --implements--> `CachePort`  [EXTRACTED]
  br/com/fiap/techchalleger/agendafacil/infrastructure/cache/RedisCachePortImpl.java → br/com/fiap/techchalleger/agendafacil/application/port/CachePort.java

## Import Cycles
- None detected.

## Communities (33 total, 0 thin omitted)

### Community 0 - "REST Controllers & Security"
Cohesion: 0.05
Nodes (60): SecurityUtils, AdminPrestadorController, AgendaController, AgendamentoController, AuthController, ContextoController, EmpresaController, EscalaController (+52 more)

### Community 1 - "Repository Ports (Agenda/Cache)"
Cohesion: 0.06
Nodes (62): AgendaItemRepositoryPort, AgendamentoRepositoryPort, AgendaRepositoryPort, CachePort, EmpresaRepositoryPort, EscalaItemRepositoryPort, EscalaRepositoryPort, ItemAgendamentoRepositoryPort (+54 more)

### Community 2 - "Agendamento Query Ports"
Cohesion: 0.06
Nodes (11): EmailMensagem, AcessoNegadoException, AgendamentoJaExistenteException, ConflitoDeHorarioPacienteException, DomainException, OperacaoInvalidaException, RegistroNaoEncontradoException, Agendamento (+3 more)

### Community 3 - "Domain Enums"
Cohesion: 0.05
Nodes (31): DiaSemanaEnum, DOMINGO, QUARTA, QUINTA, SABADO, SEGUNDA, SEXTA, TERCA (+23 more)

### Community 4 - "Profissional & Servico Ports"
Cohesion: 0.15
Nodes (20): HorarioDisponivel, SuppressWarnings, AgendamentoEnriquecido, PrestadorVinculo, UnidadeSaude, AgendaDetalhadaResponse, AgendaResponse, EscalaDetalhadaResponse (+12 more)

### Community 5 - "Escala & Agenda Save Ports"
Cohesion: 0.07
Nodes (10): Escala, EscalaItem, EscalaItemMapper, EscalaMapper, EscalaItemRepository, EscalaItemRepositoryPortImpl, Override, EscalaRepository (+2 more)

### Community 6 - "Redis Cache Infrastructure"
Cohesion: 0.07
Nodes (27): Override, RedisCachePortImpl, RedisTemplate, RedisConfig, JacksonConfig, OpenApiConfig, RabbitTemplate, RabbitConfig (+19 more)

### Community 7 - "Email & FilaEspera (Removed)"
Cohesion: 0.15
Nodes (8): EmailSenderPort, FilaEsperaRepositoryPort, PacienteRepositoryPort, ConsultarFilaEsperaUseCase, Agenda, FilaEspera, ItemAgendamento, Prestador

### Community 8 - "Agendamento Status Domain"
Cohesion: 0.10
Nodes (12): obterPorCodigo(), StatusAgendamentoEnum, AGENDADO, CANCELADO, DISPONIVEL, NAO_REALIZADO, REALIZADO, RESERVADO (+4 more)

### Community 9 - "Core Domain Models"
Cohesion: 0.29
Nodes (19): AgendaEntity, AgendaItemEntity, EmpresaEntity, EscalaEntity, EscalaItemEntity, ItemAgendamentoEntity, PrestadorEntity, PrestadorVinculoEntity (+11 more)

### Community 10 - "UnidadeSaude to Estabelecimento"
Cohesion: 0.12
Nodes (11): TipoUnidadeSaudeEnum, HOSPITAL, POSTO, UPA, UnidadeSaudeEntity, UnidadeSaudeMapper, UnidadeSaudeRepository, Override (+3 more)

### Community 11 - "Agenda Item Mappers"
Cohesion: 0.13
Nodes (10): AgendaItem, AgendaItemMapper, AgendamentoMapper, PrestadorVinculoMapper, AgendaItemRepository, AgendaItemRepositoryPortImpl, Override, org.mapstruct.Mapper (+2 more)

### Community 12 - "Mixed Application Ports"
Cohesion: 0.16
Nodes (5): SuppressWarnings, SuppressWarnings, SuppressWarnings, org.springframework.data.domain.Page, org.springframework.data.domain.Pageable

### Community 13 - "Cliente Registration"
Cohesion: 0.10
Nodes (17): CpfJaCadastradoException, SenhaFracaException, getCodigo(), obterPorCodigo(), getCodigo(), obterPorCodigo(), RoleEnum, ADMIN (+9 more)

### Community 14 - "Auth & Keycloak"
Cohesion: 0.15
Nodes (9): KeycloakTokenPort, TokenResponse, RenovarTokenUseCase, CredenciaisInvalidasException, Override, SuppressWarnings, KeycloakTokenAdapter, org.keycloak.representations.AccessTokenResponse (+1 more)

### Community 15 - "Persistence Converters"
Cohesion: 0.21
Nodes (6): Paciente, PacienteEntity, PacienteMapper, PacienteRepository, Override, PacienteRepositoryPortImpl

### Community 16 - "Empresa Domain"
Cohesion: 0.16
Nodes (3): SenhaTemporariaGenerator, java.security.SecureRandom, org.springframework.transaction.annotation.Transactional

### Community 17 - "Use Case Layer"
Cohesion: 0.18
Nodes (5): PrestadorVinculoItemAgd, PrestadorVinculoItemAgdMapper, PrestadorVinculoItemAgdRepository, Override, PrestadorVinculoItemAgdRepositoryPortImpl

### Community 18 - "Escala Domain"
Cohesion: 0.20
Nodes (5): Empresa, EmpresaMapper, EmpresaRepository, EmpresaRepositoryPortImpl, Override

### Community 19 - "Spring Config & OpenAPI"
Cohesion: 0.19
Nodes (4): AgendaMapper, AgendaRepository, AgendaRepositoryPortImpl, Override

### Community 20 - "Scheduler Jobs"
Cohesion: 0.22
Nodes (4): PrestadorMapper, PrestadorRepository, Override, PrestadorRepositoryPortImpl

### Community 21 - "Keycloak Admin Adapter"
Cohesion: 0.23
Nodes (3): PrestadorVinculoRepository, Override, PrestadorVinculoRepositoryPortImpl

### Community 22 - "Error Handling"
Cohesion: 0.33
Nodes (4): AccessDeniedException, ErroResponse, RestExceptionHandler, org.springframework.web.bind.annotation.ExceptionHandler

### Community 23 - "Agendamento Use Cases"
Cohesion: 0.21
Nodes (5): EmailJaCadastradoException, ServicoIndisponivelException, Override, KeycloakAdminAdapter, org.keycloak.admin.client.Keycloak

### Community 24 - "Domain Services"
Cohesion: 0.30
Nodes (9): ContextoUnidadeFilter, Override, Override, SincronizarUsuarioFilter, jakarta.servlet.FilterChain, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, lombok.extern.slf4j.Slf4j (+1 more)

### Community 25 - "REST DTOs"
Cohesion: 0.31
Nodes (6): Usuario, UsuarioEntity, UsuarioMapper, UsuarioRepository, Override, UsuarioRepositoryPortImpl

### Community 26 - "FilaEspera Use Cases (Removed)"
Cohesion: 0.31
Nodes (7): Override, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatusCode, org.springframework.web.bind.annotation.RestControllerAdvice, org.springframework.web.bind.MethodArgumentNotValidException, org.springframework.web.context.request.WebRequest, org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

### Community 27 - "Liquibase Migrations"
Cohesion: 0.43
Nodes (6): Override, LocalTimeModelConverter, io.swagger.v3.core.converter.AnnotatedType, io.swagger.v3.core.converter.ModelConverter, io.swagger.v3.core.converter.ModelConverterContext, io.swagger.v3.oas.models.media.Schema

### Community 28 - "RabbitMQ Config"
Cohesion: 0.43
Nodes (4): EmailConsumer, org.springframework.amqp.rabbit.annotation.RabbitListener, org.springframework.mail.javamail.JavaMailSender, org.thymeleaf.TemplateEngine

### Community 29 - "Prestador Use Cases"
Cohesion: 0.39
Nodes (3): ItemAgendamentoMapper, ItemAgendamentoRepositoryPortImpl, Override

### Community 30 - "Vinculos Use Cases"
Cohesion: 0.39
Nodes (3): ItemAgendamentoRepository, org.springframework.data.jpa.repository.JpaRepository, org.springframework.data.jpa.repository.Query

### Community 31 - "Email Templates"
Cohesion: 0.52
Nodes (5): Override, KeycloakRolesConverter, org.springframework.core.convert.converter.Converter, org.springframework.security.core.GrantedAuthority, org.springframework.security.oauth2.jwt.Jwt

### Community 32 - "Senha & Token Utils"
Cohesion: 0.60
Nodes (3): AgendaFacilApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.scheduling.annotation.EnableScheduling

## Knowledge Gaps
- **23 isolated node(s):** `DOMINGO`, `SEGUNDA`, `TERCA`, `QUARTA`, `QUINTA` (+18 more)
  These have ≤1 connection - possible missing edges or undocumented components.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `RegistroNaoEncontradoException` connect `Agendamento Query Ports` to `Repository Ports (Agenda/Cache)`, `Profissional & Servico Ports`, `Escala & Agenda Save Ports`, `Email & FilaEspera (Removed)`, `Mixed Application Ports`, `Empresa Domain`, `Use Case Layer`, `Error Handling`, `FilaEspera Use Cases (Removed)`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Why does `DiaSemanaEnum` connect `Domain Enums` to `Repository Ports (Agenda/Cache)`, `Profissional & Servico Ports`, `Escala & Agenda Save Ports`, `Email & FilaEspera (Removed)`, `Core Domain Models`, `Cliente Registration`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._
- **Why does `FilaEspera` connect `Email & FilaEspera (Removed)` to `REST Controllers & Security`, `Agendamento Query Ports`, `Domain Enums`, `Profissional & Servico Ports`, `Core Domain Models`?**
  _High betweenness centrality (0.033) - this node is a cross-community bridge._
- **What connects `DOMINGO`, `SEGUNDA`, `TERCA` to the rest of the system?**
  _23 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `REST Controllers & Security` be split into smaller, more focused modules?**
  _Cohesion score 0.05089448703906535 - nodes in this community are weakly interconnected._
- **Should `Repository Ports (Agenda/Cache)` be split into smaller, more focused modules?**
  _Cohesion score 0.05999250093738283 - nodes in this community are weakly interconnected._
- **Should `Agendamento Query Ports` be split into smaller, more focused modules?**
  _Cohesion score 0.06291466483642187 - nodes in this community are weakly interconnected._