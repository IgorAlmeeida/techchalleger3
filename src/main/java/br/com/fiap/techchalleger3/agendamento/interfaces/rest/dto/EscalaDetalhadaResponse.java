package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "Escala recorrente detalhada com todos os serviços")
public record EscalaDetalhadaResponse(
        @Schema(description = "Identificador único da escala", example = "5")
        Integer id,

        @Schema(description = "Vínculo profissional/estabelecimento desta escala")
        ProfissionalVinculoResumo profissionalVinculo,

        @Schema(description = "Dia da semana em que o profissional atende conforme esta escala", example = "SEGUNDA")
        DiaSemanaEnum diaSemana,

        @JsonFormat(pattern = "HH:mm:ss") LocalTime horaInicio,

        @JsonFormat(pattern = "HH:mm:ss") LocalTime horaFim,

        @Schema(description = "Serviços oferecidos nesta escala")
        List<ServicoResumo> servicos
) {}
