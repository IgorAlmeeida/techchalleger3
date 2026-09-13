# Deploy no Azure Container Apps

Implementa a Spec 08 (`02-Specs/08-Spec-Deploy-Azure.md` no vault). Dois Container Apps separados (`ca-agendamento` público, `ca-keycloak` interno) + um PostgreSQL Flexible Server (free tier) com dois databases.

## Pré-requisitos

- Azure CLI instalado e logado (`az login`).
- Conta Azure com o free tier disponível (novo cliente) para o Postgres Flexible Server sair sem custo (750h/mês, 32 GB, por 12 meses).
- Imagem já publicada no GHCR pelo workflow do GitHub Actions (job `docker-publish`, só roda em push na `main`).
- **Antes de rodar:** confirmar os dois pontos em aberto da Spec 08 — se o Keycloak precisa de ingress externo (seção 2) e se o pacote no GHCR está público ou privado (seção 5).

## Passo a passo

```bash
# 1. Criar o resource group
az group create --name rg-techchallenger3 --location brazilsouth

# 2. Validar o template antes de aplicar (dry-run)
az deployment group validate \
  --resource-group rg-techchallenger3 \
  --template-file main.bicep \
  --parameters postgresAdminPassword='<senha-forte>' \
               keycloakAdminPassword='<senha-forte>' \
               gmailUsername='<email>' \
               gmailAppPassword='<senha-de-app-do-gmail>' \
               keycloakClientSecret='<secret-do-client-keycloak>'

# 3. Aplicar de fato
az deployment group create \
  --resource-group rg-techchallenger3 \
  --template-file main.bicep \
  --parameters postgresAdminPassword='<senha-forte>' \
               keycloakAdminPassword='<senha-forte>' \
               gmailUsername='<email>' \
               gmailAppPassword='<senha-de-app-do-gmail>' \
               keycloakClientSecret='<secret-do-client-keycloak>'
```

Se o pacote do GHCR for **privado**, adicione `ghcrIsPublic=false` aos parâmetros e edite o `main.bicep` para passar o PAT real no lugar do placeholder `__SUBSTITUA_PELO_PAT_COM_read:packages__` (idealmente via parâmetro `@secure()` novo, não hardcoded no arquivo).

## Depois de aplicar

1. Pegar a URL pública: `az containerapp show -n ca-agendamento -g rg-techchallenger3 --query properties.configuration.ingress.fqdn -o tsv`
2. Rodar as migrations do Liquibase acontece automaticamente no boot da aplicação (`ddl-auto: validate` + Liquibase changelog já no classpath) — não precisa de passo manual.
3. Testar login/health: `curl https://<fqdn>/actuator/health` (se Actuator estiver habilitado) ou um endpoint público qualquer da API.
4. Verificar logs: `az containerapp logs show -n ca-agendamento -g rg-techchallenger3 --follow`

## O que este template NÃO cobre (fora do escopo desta primeira versão)

- Deploy contínuo (CD) automatizado do GitHub Actions para o Azure — hoje é aplicação manual do Bicep. Dá pra evoluir depois com `az deployment group create` rodando como job do workflow, autenticando via OIDC (federação GitHub↔Azure, sem secret de longa duração).
- Custom domain / certificado próprio.
- Keycloak em modo produção (`start` em vez de `start-dev`) — ver Spec 08, seção 3.
- Alertas/monitoramento além do Log Analytics básico já conectado ao Container Apps Environment.
