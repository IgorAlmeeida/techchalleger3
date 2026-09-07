package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agendamento {
    private Integer id;
    private Integer agendaId;
    private LocalDate dataAgenda;
    private Integer agendamentoPaiId;
    private Integer servicoId;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Integer clienteId;
    private StatusAgendamentoEnum status;
    private Boolean presencaConfirmada;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;

    public boolean isDisponivel() {
        return StatusAgendamentoEnum.DISPONIVEL.equals(this.status);
    }
}
