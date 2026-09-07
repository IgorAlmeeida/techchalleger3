# language: pt
@core
Funcionalidade: Agendamento de serviço
  Como cliente
  Quero agendar um serviço em um estabelecimento
  Para reservar meu horário com o profissional escolhido

  Contexto:
    Dado que existe o estabelecimento "Studio Bella" com o serviço "Corte Feminino"
    E o profissional "Ana" atende esse serviço com escala às terças das "09:00" às "12:00"

  Cenário: Cliente agenda um horário disponível
    Dado que o cliente "Maria" está autenticado
    Quando ela solicita um agendamento para "Corte Feminino" com "Ana" na próxima terça às "09:00"
    Então o agendamento deve ser confirmado com status "AGENDADO"
    E uma notificação de confirmação deve ser enviada para Maria

  Cenário: Cliente tenta agendar um horário já ocupado
    Dado que o horário de terça às "09:00" com "Ana" já está agendado por outro cliente
    Quando o cliente "Maria" tenta agendar o mesmo horário
    Então o sistema deve informar que não há disponibilidade
