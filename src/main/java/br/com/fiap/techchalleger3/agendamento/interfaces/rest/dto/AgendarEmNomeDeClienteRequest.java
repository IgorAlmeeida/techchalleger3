package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AgendarEmNomeDeClienteRequest(
        @NotNull Integer profissionalVinculoId,
        @NotNull Integer servicoId,
        Integer agendamentoId,
        @NotBlank String cpf,
        String nome,
        LocalDate dataNascimento,
        String telefone,
        String sexo,
        String endereco,
        String email
) {}
