package br.com.fiap.techchalleger3.agendamento.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum RoleEnum {

    ADMIN("ADMIN", "Administrador"),
    PROFISSIONAL("PROFISSIONAL", "Profissional"),
    CLIENTE("CLIENTE", "Cliente");

    private final String codigo;
    private final String descricao;

    @JsonValue
    public String getCodigo() {
        return codigo;
    }

    public static RoleEnum obterPorCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(r -> r.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role inválida: " + codigo));
    }
}
