# language: pt
@qualidade
Funcionalidade: Gerenciamento de agenda pelo estabelecimento
  Como estabelecimento
  Quero cancelar uma agenda inteira de um profissional
  Para lidar com imprevistos sem cancelar agendamento por agendamento

  Contexto:
    Dado que existe uma agenda de id 5 com agendamentos confirmados

  Cenário: Estabelecimento cancela a agenda do dia
    Dado que o admin está autenticado como ADMIN
    Quando o estabelecimento cancela a agenda de id 5
    Então a agenda deve ser cancelada com sucesso

  Cenário: Profissional tenta cancelar agenda de outro profissional
    Dado que um profissional não autorizado está autenticado como PROFISSIONAL
    Quando esse profissional tenta cancelar a agenda de id 5
    Então o sistema deve negar o acesso ao cancelamento da agenda
