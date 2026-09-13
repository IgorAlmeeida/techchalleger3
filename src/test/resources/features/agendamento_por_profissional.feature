# language: pt
@core
Funcionalidade: Agendamento feito pelo estabelecimento em nome do cliente
  Como profissional/atendente do estabelecimento
  Quero registrar um agendamento em nome de um cliente
  Para atender clientes que ligaram ou chegaram sem usar o app

  Contexto:
    Dado que existe o estabelecimento "Studio Bella" com o serviço "Corte Feminino"
    E o profissional "Ana" atende esse serviço com escala às terças das "09:00" às "12:00"
    E a cliente "Joana" já está cadastrada no sistema

  Cenário: Atendente registra um agendamento em nome de uma cliente existente
    Dado que o atendente do "Studio Bella" está autenticado como PROFISSIONAL
    Quando ele agenda "Corte Feminino" com "Ana" para a cliente "Joana" na próxima terça
    Então o agendamento em nome do cliente deve ser confirmado com status "AGENDADO"
    E o agendamento deve estar associado à cliente "Joana"
    E uma notificação de confirmação deve ser enviada para Joana

  Cenário: Atendente tenta agendar em horário já ocupado
    Dado que o horário com "Ana" já está agendado por outro cliente
    Quando o atendente tenta agendar "Corte Feminino" para a cliente "Joana" no mesmo horário ocupado
    Então o sistema deve informar que não há disponibilidade para o agendamento em nome
