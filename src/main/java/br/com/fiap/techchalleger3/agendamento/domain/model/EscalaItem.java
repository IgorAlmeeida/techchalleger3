package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Item de escala: um dia da semana e o intervalo de horário de trabalho do profissional.
 */
public class EscalaItem {
    private Integer id;
    private Integer escalaId;
    private Integer servicoId;
    private Boolean ativa;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;
}
