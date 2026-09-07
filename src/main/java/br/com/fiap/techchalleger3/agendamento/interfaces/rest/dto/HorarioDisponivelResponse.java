package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Horário disponível para agendamento")
public record HorarioDisponivelResponse(
        @Schema(description = "Identificador da agenda à qual este slot pertence", example = "10")
        Integer agendaId,

        @Schema(description = "Identificador do slot de agendamento disponível", example = "42")
        Integer agendamentoId,

        @Schema(description = "Data da agenda", example = "2026-08-05")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataAgenda,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaInicio,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime horaFim,

        @Schema(description = "Profissional que atende neste slot")
        ProfissionalResumo profissional,

        @Schema(description = "Estabelecimento onde ocorre o atendimento")
        EstabelecimentoResumo estabelecimento,

        @Schema(description = "Serviço oferecido neste slot")
        ServicoResumo servico
) {}
