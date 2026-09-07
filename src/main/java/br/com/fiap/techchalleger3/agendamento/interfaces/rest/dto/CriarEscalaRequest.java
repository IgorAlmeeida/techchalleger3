package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "Dados para criação de uma escala recorrente de disponibilidade")
public record CriarEscalaRequest(
        @Schema(description = "Identificador do vínculo profissional/estabelecimento para o qual a escala é criada", example = "1")
        @NotNull Integer profissionalVinculoId,

        @Schema(description = "Dia da semana em que o profissional atende conforme esta escala", example = "SEGUNDA",
                allowableValues = {"SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO", "DOMINGO"})
        @NotNull DiaSemanaEnum diaSemana,

        @NotNull @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaInicio,

        @NotNull @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaFim,

        @Schema(description = "Lista dos identificadores dos serviços oferecidos nesta escala", example = "[2, 3]")
        @NotNull List<Integer> servicosIds
) {}
