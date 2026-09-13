# JMeter — Plano de Carga (10 cenários)

## Pré-requisitos

- JMeter 5.6+ (`jmeter` no PATH)
- Aplicação rodando e acessível na URL alvo
- `seed.sh` já executado contra a mesma `BASE_URL` (gera estabelecimento / profissional / vínculo / agenda)

## Variáveis de projeto

| Variável | Default (GUI) | Descrição |
|---|---|---|
| `protocol` | `http` | `http` ou `https` |
| `host` | `localhost` | FQDN ou IP da aplicação |
| `port` | `8080` | Porta |
| `vinculoId` | `1` | ID do vínculo profissional gerado pelo seed |
| `servicoId` | `1` | ID do serviço gerado pelo seed |
| `estabelecimentoId` | `1` | ID do estabelecimento gerado pelo seed |
| `adminUser` | `admin` | Usuário admin no Keycloak |
| `adminPass` | `admin` | Senha do admin |

Edite os defaults na GUI (TestPlan → User Defined Variables) ou passe via `-J` na linha de comando.

## Como rodar — localhost (smoke test)

```bash
# 1. Suba a aplicação localmente
docker compose up -d

# 2. Execute o seed para criar os dados base
cd infra/load-test
BASE_URL=http://localhost:8080 bash seed.sh

# 3. Rode o plano (GUI — validação visual)
jmeter -t infra/load-test/jmeter/agendamento-plano-carga.jmx

# 4. Rode headless com saída de relatório
jmeter -n \
  -t infra/load-test/jmeter/agendamento-plano-carga.jmx \
  -l resultados.jtl \
  -e -o relatorio-html/
```

## Como rodar — Azure (pós Spec 08)

```bash
jmeter -n \
  -t infra/load-test/jmeter/agendamento-plano-carga.jmx \
  -Jprotocol=https \
  -Jhost=<FQDN-do-Container-App> \
  -Jport=443 \
  -JvinculoId=<id-do-seed> \
  -JservicoId=<id-do-seed> \
  -JestabelecimentoId=<id-do-seed> \
  -JadminUser=admin \
  -JadminPass=<senha-admin-keycloak> \
  -l resultados-azure.jtl \
  -e -o relatorio-azure/
```

Guarde `resultados-azure.jtl` e a pasta `relatorio-azure/` como evidência para o relatório técnico.

## Os 10 cenários

| # | Nome | Threads | Ramp | Loops | Auth |
|---|---|---|---|---|---|
| 01 | Login concorrente | 30 | 10s | 1 | — |
| 02 | Cadastro de clientes em massa | 30 | 15s | 1 | — |
| 03 | Busca pública de estabelecimentos | 50 | 10s | 3 | — |
| 04 | Listagem de oferta do estabelecimento | 20 | 10s | 3 | CLIENTE |
| 05 | Criação concorrente de agendamentos | 20 | 15s | 1 | CLIENTE |
| 06 | Listar meus agendamentos | 20 | 10s | 3 | CLIENTE |
| 07 | Cancelamento concorrente | 15 | 10s | 1 | CLIENTE |
| 08 | Listagem de agendamentos do profissional | 10 | 5s | 3 | ADMIN |
| 09 | Spike na criação de agendamentos | 50 | **5s** | 1 | CLIENTE |
| 10 | Soak test misto (10 min) | 10 | 30s | ∞ (600s) | CLIENTE |

**Cenários 05 e 09** aceitam `201`, `409` e `422` como respostas válidas de negócio
(ResponseAssertion com regex `201|409|422`). Apenas `5xx` e timeouts contam como falha.

**Cenário 07** cria um agendamento por thread (POST) e logo em seguida cancela (PATCH)
— o `agendamentoId` é extraído via JSONPathExtractor (`$.id`) da resposta do POST.

**Cenário 10** usa scheduler JMeter (duration=600s) e loop infinito — encerra
automaticamente após 10 minutos.

## Dados de teste gerados dinamicamente

Cenários com autenticação de CLIENTE geram CPF válido + email único por thread usando
um JSR223 Sampler (Groovy) com o algoritmo de dígito verificador do CPF. Isso evita
conflitos de CPF/email entre threads paralelas.

## Atenção antes de rodar contra produção

1. Abra o `.jmx` no JMeter GUI e confirme que carrega sem erro.
2. Rode pelo menos uma vez contra `localhost` com a app de pé.
3. Desative cenários que não quer executar (botão direito → Disable no Thread Group).
4. O soak test (TG10) cria contas novas a cada iteração — limpe o banco de teste após rodar.
