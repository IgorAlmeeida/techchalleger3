package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "Agenda detalhada com todos os serviços do dia")
public record AgendaDetalhadaResponse(
        @Schema(description = "Identificador único da agenda", example = "10")
        Integer id,

        @Schema(description = "Data concreta desta agenda", example = "2026-08-05")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataAgenda,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaInicio,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaFim,

        @Schema(description = "Vínculo profissional/estabelecimento responsável por esta agenda")
        ProfissionalVinculoResumo profissionalVinculo,

        @Schema(description = "Lista de serviços disponíveis nesta agenda")
        List<ServicoResumo> servicos
) {}
