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
public class Agenda {
    private Integer id;
    private Integer escalaId;
    private LocalDate dataAgenda;
    private DiaSemanaEnum diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Integer estabelecimentoId;
    private Integer profissionalVinculoId;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;
}
