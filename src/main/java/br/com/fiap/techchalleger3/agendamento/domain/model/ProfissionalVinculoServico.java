package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Associação entre um vínculo profissional e um serviço ofertado.
 * Indica quais serviços um profissional realiza em determinado vínculo.
 */
public class ProfissionalVinculoServico {
    private Integer id;
    private Integer profissionalVinculoId;
    private Integer servicoId;
    private BigDecimal tarifa;
}
