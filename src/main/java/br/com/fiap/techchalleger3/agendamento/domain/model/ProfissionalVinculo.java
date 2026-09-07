package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalVinculo {
    private Integer id;
    private Integer profissionalId;
    private Integer estabelecimentoId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;

    public boolean isAtivo() {
        return dataFim == null;
    }
}
