package br.com.fiap.techchalleger3.agendamento.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum StatusAgendamentoEnum {

    DISPONIVEL("DISPONIVEL", "Disponível"),
    RESERVADO("RESERVADO", "Reservado"),
    AGENDADO("AGENDADO", "Agendado"),
    REALIZADO("REALIZADO", "Realizado"),
    NAO_REALIZADO("NAO_REALIZADO", "Não Realizado"),
    CANCELADO("CANCELADO", "Cancelado");

    private final String codigo;
    private final String descricao;

    @JsonValue
    public String getCodigo() {
        return codigo;
    }

    public static StatusAgendamentoEnum obterPorCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(s -> s.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status de agendamento inválido: " + codigo));
    }
}
