package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Escala {
    private Integer id;
    private Integer profissionalVinculoId;
    private Integer estabelecimentoId;
    private DiaSemanaEnum diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;

    public boolean conflitaCom(Escala outra) {
        if (!this.profissionalVinculoId.equals(outra.profissionalVinculoId)) {
            return false;
        }
        if (!this.diaSemana.equals(outra.diaSemana)) {
            return false;
        }
        return this.horaInicio.isBefore(outra.horaFim) && outra.horaInicio.isBefore(this.horaFim);
    }
}
