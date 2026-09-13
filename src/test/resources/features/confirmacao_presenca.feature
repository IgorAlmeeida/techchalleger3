# language: pt
@core
Funcionalidade: Confirmação de presença e não comparecimento
  Como estabelecimento
  Quero registrar se o cliente compareceu ao agendamento
  Para manter o painel de controle da agenda atualizado

  Contexto:
    Dado que existe um agendamento ativo de id 10 com status "AGENDADO"

  Cenário: Estabelecimento confirma o comparecimento do cliente
    Dado que o profissional está autenticado como PROFISSIONAL
    Quando o estabelecimento confirma a presença no agendamento de id 10
    Então a confirmação de presença deve ser registrada com sucesso

  Cenário: Estabelecimento tenta confirmar agendamento inexistente
    Dado que o profissional está autenticado como PROFISSIONAL
    Quando o estabelecimento tenta confirmar a presença no agendamento de id 999
    Então o sistema deve informar que o agendamento não foi encontrado
