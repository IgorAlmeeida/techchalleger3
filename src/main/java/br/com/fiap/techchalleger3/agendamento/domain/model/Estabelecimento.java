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
public class Estabelecimento {
    private Integer id;
    private String nome;
    private String cnpj;
    private String endereco;
    private String telefone;
    private String responsavelNome;
    private String responsavelCpf;
    private List<String> fotosUrls;
    private Boolean ativo;
    private LocalDateTime dhInsert;
    private LocalDateTime dhAtualizacao;
}
