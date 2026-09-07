package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "Dados para atualização de uma escala recorrente de disponibilidade")
public record AtualizarEscalaRequest(
        @Schema(description = "Dia da semana em que o profissional atende conforme esta escala", example = "SEGUNDA",
                allowableValues = {"SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO", "DOMINGO"})
        @NotNull DiaSemanaEnum diaSemana,

        @NotNull @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaInicio,

        @NotNull @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaFim,

        @Schema(description = "Lista dos identificadores dos serviços (substitui a lista anterior por completo)", example = "[2, 3]")
        @NotNull List<Integer> servicosIds
) {}
