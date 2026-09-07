package br.com.fiap.techchalleger3.agendamento.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum DiaSemanaEnum {

    DOMINGO("DOMINGO", "Domingo"),
    SEGUNDA("SEGUNDA", "Segunda-feira"),
    TERCA("TERCA", "Terça-feira"),
    QUARTA("QUARTA", "Quarta-feira"),
    QUINTA("QUINTA", "Quinta-feira"),
    SEXTA("SEXTA", "Sexta-feira"),
    SABADO("SABADO", "Sábado");

    private final String codigo;
    private final String descricao;

    @JsonValue
    public String getCodigo() {
        return codigo;
    }

    public static DiaSemanaEnum obterPorCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(d -> d.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dia da semana inválido: " + codigo));
    }
}
