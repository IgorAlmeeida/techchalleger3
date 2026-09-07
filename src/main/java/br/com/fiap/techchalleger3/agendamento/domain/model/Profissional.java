package br.com.fiap.techchalleger3.agendamento.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profissional {
    private Integer id;
    private Integer usuarioId;
    private String nome;
    private String email;
    private List<String> especialidades;
    private String endereco;
    @Builder.Default
    private Boolean ativo = true;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;
}
