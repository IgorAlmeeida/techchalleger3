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
 * Item de uma agenda: define quais serviços ({@link Servico}) estão disponíveis naquela agenda.
 */
public class AgendaItem {
    private Integer id;
    private Integer agendaId;
    private Integer servicoId;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;
}
