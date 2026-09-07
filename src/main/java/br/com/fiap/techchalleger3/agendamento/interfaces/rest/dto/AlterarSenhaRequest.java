package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record AlterarSenhaRequest(
        @NotBlank String senhaAtual,
        @NotBlank String senhaNova
) {}
