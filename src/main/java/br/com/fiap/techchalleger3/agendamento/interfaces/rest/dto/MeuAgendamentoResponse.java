package br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto;

import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record MeuAgendamentoResponse(
        Integer id,
        Integer agendaId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dataAgenda,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime horaInicio,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime horaFim,
        StatusAgendamentoEnum status,
        ServicoResumo servico,
        ProfissionalResumo profissional,
        ClienteResumo cliente,
        EstabelecimentoResumo estabelecimento,
        Boolean presencaConfirmada
) {}
