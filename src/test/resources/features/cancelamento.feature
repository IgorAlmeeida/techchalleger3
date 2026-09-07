# language: pt
@core
Funcionalidade: Cancelamento de agendamento

  Cenário: Cliente cancela um agendamento futuro
    Dado que "Maria" tem um agendamento confirmado para amanhã
    Quando ela cancela esse agendamento
    Então o status do agendamento deve mudar para "CANCELADO"
    E o horário deve voltar a aparecer como disponível na busca
