package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Servico {
    private Integer id;
    private String nome;
    private Integer duracaoMinutos;
    private BigDecimal preco;
    private Boolean ativo;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;
}
