// =====================================================================
// Teste de carga: criação concorrente de agendamentos.
// Cenário mais crítico do sistema — valida se a checagem de conflito de
// horário aguenta concorrência real (não só o que o teste unitário cobre).
//
// Pré-requisito: rodar seed.sh antes, e passar VINCULO_ID/SERVICO_ID gerados.
//
// Uso:
//   k6 run -e BASE_URL=http://localhost:8080 -e VINCULO_ID=1 -e SERVICO_ID=1 booking-load-test.js
// =====================================================================
import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VINCULO_ID = __ENV.VINCULO_ID;
const SERVICO_ID = __ENV.SERVICO_ID;
const NUM_CLIENTES = parseInt(__ENV.NUM_CLIENTES || '60', 10);

if (!VINCULO_ID || !SERVICO_ID) {
  throw new Error('VINCULO_ID e SERVICO_ID são obrigatórios (rode seed.sh antes e passe os ids gerados).');
}

const unexpectedStatus = new Counter('unexpected_status');

export const options = {
  scenarios: {
    criacao_concorrente: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: 20 },  // rampa até 20 usuários simultâneos
        { duration: '30s', target: 20 },  // sustenta a carga
        { duration: '10s', target: 0 },   // rampa de volta
      ],
    },
  },
  thresholds: {
    // p95 de latência abaixo de 800ms — ajuste depois de rodar a primeira vez e ter uma baseline real
    http_req_duration: ['p(95)<800'],
    // status realmente inesperado (5xx, timeout) tem que ser raríssimo — 409/422 são respostas de negócio esperadas
    unexpected_status: ['count<5'],
  },
};

// Gera um CPF com dígitos verificadores válidos (algoritmo padrão mod 11),
// para não esbarrar em validação de CPF ao cadastrar os clientes de teste.
function gerarCpfValido() {
  const n = Array.from({ length: 9 }, () => Math.floor(Math.random() * 10));
  const calcDigito = (base) => {
    let soma = 0;
    let peso = base.length + 1;
    for (const d of base) {
      soma += d * peso--;
    }
    const resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  };
  const d1 = calcDigito(n);
  const d2 = calcDigito([...n, d1]);
  return [...n, d1, d2].join('');
}

// setup() roda uma única vez, antes das VUs — cria o pool de clientes de teste
// e já autentica cada um (evita autenticar a cada iteração e distorcer a métrica).
export function setup() {
  const clientes = [];
  for (let i = 0; i < NUM_CLIENTES; i++) {
    const email = `loadtest.cliente.${Date.now()}.${i}@example.com`;
    const senha = 'SenhaLoadTest@123';

    const cadastroRes = http.post(
      `${BASE_URL}/api/auth/cadastrar-cliente`,
      JSON.stringify({
        nome: `Cliente Load Test ${i}`,
        email,
        password: senha,
        cpf: gerarCpfValido(),
        dataNascimento: '1995-01-01',
        telefone: '(11) 90000-0000',
        sexo: 'M',
        endereco: 'Rua de Teste, 100',
      }),
      { headers: { 'Content-Type': 'application/json' } }
    );

    if (cadastroRes.status !== 201) {
      console.warn(`Falha ao cadastrar cliente ${i}: ${cadastroRes.status} ${cadastroRes.body}`);
      continue;
    }

    const loginRes = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ username: email, password: senha }),
      { headers: { 'Content-Type': 'application/json' } }
    );

    if (loginRes.status === 200) {
      clientes.push({ token: loginRes.json('accessToken') });
    }
  }

  if (clientes.length === 0) {
    throw new Error('Nenhum cliente de teste pôde ser criado/autenticado — abortando.');
  }

  return { clientes };
}

export default function (data) {
  const cliente = data.clientes[__VU % data.clientes.length];

  const res = http.post(
    `${BASE_URL}/api/agendamentos`,
    JSON.stringify({
      profissionalVinculoId: parseInt(VINCULO_ID, 10),
      servicoId: parseInt(SERVICO_ID, 10),
      dataPreferencia: new Date().toISOString().slice(0, 10),
      agendamentoId: null,
    }),
    {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${cliente.token}`,
      },
    }
  );

  // 201 = agendou; 409 = já tinha agendamento ativo (regra de negócio, esperado sob reuso
  // de VU); 422 = sem horário disponível (esperado depois que os slots do seed esgotarem).
  // Qualquer outra coisa (5xx, timeout) é falha real do sistema sob carga.
  const esperado = check(res, {
    'status é 201, 409 ou 422 (resposta de negócio válida)': (r) =>
      [201, 409, 422].includes(r.status),
  });

  if (!esperado) {
    unexpectedStatus.add(1);
    console.error(`Status inesperado: ${res.status} — ${res.body}`);
  }
}
