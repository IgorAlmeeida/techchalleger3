# language: pt
@qualidade
Funcionalidade: Avaliação de atendimento

  Cenário: Cliente avalia um atendimento concluído
    Dado que "Maria" teve um agendamento com status "REALIZADO"
    Quando ela avalia o atendimento com nota 5 e comentário "Excelente"
    Então a avaliação deve ser registrada
    E a nota média do profissional deve ser atualizada

  Cenário: Cliente não pode avaliar atendimento não realizado
    Dado que "Maria" tem um agendamento com status "AGENDADO" ainda não ocorrido
    Quando ela tenta avaliar esse atendimento
    Então o sistema deve rejeitar a avaliação
