package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegracaoCalendarioExterno {
    private Integer id;
    private Integer clienteId;
    private Integer profissionalId;
    private ProvedorCalendarioEnum provedor;
    private String accessToken;
    private String refreshToken;
    private Instant expiraEm;
    private Boolean ativo;
}
