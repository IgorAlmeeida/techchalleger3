package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {
    private Integer id;
    private Integer agendamentoId;
    private Integer clienteId;
    private Integer estabelecimentoId;
    private Integer profissionalVinculoId;
    private int nota;
    private String comentario;
    private LocalDateTime dhInsert;
}
