#!/usr/bin/env bash
# =====================================================================
# Seed de dados para o teste de carga (Spec de testes não funcionais).
# Cria 1 estabelecimento, 1 serviço, 1 profissional, o vínculo entre eles,
# 1 escala recorrente (segunda-feira, 08:00-18:00) e gera agendas concretas
# (slots reais) para as próximas 5 semanas — o suficiente para um teste
# de carga de criação concorrente de agendamentos.
#
# Requer: curl, jq
# Uso: BASE_URL=http://localhost:8080 ./seed.sh
# Saída: exporta VINCULO_ID e SERVICO_ID para $GITHUB_OUTPUT (se definido)
#        ou imprime na tela (uso local).
# =====================================================================
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "==> Login como admin"
# Credenciais do AdminSeeder (infrastructure/config/AdminSeeder.java) — criadas
# automaticamente no primeiro boot da aplicação, se ainda não existir esse usuário.
ADMIN_TOKEN=$(curl -sf -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@agendamento.com","password":"Admin@1234"}' | jq -r '.accessToken')

auth() { curl -sf -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" "$@"; }

echo "==> Criando estabelecimento"
ESTABELECIMENTO_ID=$(auth -X POST "$BASE_URL/api/estabelecimentos" -d '{
  "nome": "Studio Load Test",
  "cnpj": "'"$(date +%s | tail -c 9)"'000100",
  "endereco": "Rua de Teste, 100",
  "telefone": "(11) 90000-0000",
  "responsavelNome": "Responsavel Teste",
  "responsavelCpf": "52998224725",
  "fotosUrls": []
}' | jq -r '.id')
echo "    estabelecimentoId=$ESTABELECIMENTO_ID"

echo "==> Criando serviço (30 min)"
SERVICO_ID=$(auth -X POST "$BASE_URL/api/servicos" -d '{
  "nome": "Corte Load Test",
  "duracaoMinutos": 30,
  "preco": 50.00
}' | jq -r '.id')
echo "    servicoId=$SERVICO_ID"

echo "==> Criando profissional"
PROFISSIONAL_ID=$(auth -X POST "$BASE_URL/api/admin/profissionais" -d '{
  "nome": "Profissional Load Test",
  "email": "profissional.loadtest.'"$(date +%s)"'@example.com",
  "especialidades": ["Corte"],
  "endereco": "Rua de Teste, 100"
}' | jq -r '.id')
echo "    profissionalId=$PROFISSIONAL_ID"

echo "==> Criando vínculo profissional<->estabelecimento"
VINCULO_ID=$(auth -X POST "$BASE_URL/api/profissional-vinculos" -d '{
  "profissionalId": '"$PROFISSIONAL_ID"',
  "estabelecimentoId": '"$ESTABELECIMENTO_ID"',
  "dataInicio": "'"$(date +%Y-%m-%d)"'"
}' | jq -r '.id')
echo "    vinculoId=$VINCULO_ID"

echo "==> Associando serviço ao vínculo"
auth -X POST "$BASE_URL/api/profissional-vinculos/$VINCULO_ID/itens" -d '{
  "servicoId": '"$SERVICO_ID"'
}' > /dev/null

echo "==> Criando escala (SEGUNDA, 08:00-18:00)"
ESCALA_ID=$(auth -X POST "$BASE_URL/api/escalas" -d '{
  "profissionalVinculoId": '"$VINCULO_ID"',
  "diaSemana": "SEGUNDA",
  "horaInicio": "08:00:00",
  "horaFim": "18:00:00",
  "servicosIds": ['"$SERVICO_ID"']
}' | jq -r '.id')
echo "    escalaId=$ESCALA_ID"

echo "==> Gerando agendas concretas (próximas 5 semanas)"
DATA_INICIO=$(date +%Y-%m-%d)
DATA_FIM=$(date -d "+35 days" +%Y-%m-%d 2>/dev/null || date -v+35d +%Y-%m-%d) # GNU date (Linux) ou BSD date (mac)
auth -X POST "$BASE_URL/api/agendas/gerar" -d '{
  "escalaId": '"$ESCALA_ID"',
  "dataInicio": "'"$DATA_INICIO"'",
  "dataFim": "'"$DATA_FIM"'"
}' > /dev/null

echo "==> Seed concluído: VINCULO_ID=$VINCULO_ID SERVICO_ID=$SERVICO_ID"

if [ -n "${GITHUB_OUTPUT:-}" ]; then
  echo "vinculo_id=$VINCULO_ID" >> "$GITHUB_OUTPUT"
  echo "servico_id=$SERVICO_ID" >> "$GITHUB_OUTPUT"
fi
