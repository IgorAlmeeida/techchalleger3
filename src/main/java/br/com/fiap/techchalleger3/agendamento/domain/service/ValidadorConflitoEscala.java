package br.com.fiap.techchalleger3.agendamento.domain.service;

import br.com.fiap.techchalleger3.agendamento.domain.exception.ConflitoDeEscalaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;

import java.util.List;

public class ValidadorConflitoEscala {

    public void validar(Escala nova, List<Escala> existentes) {
        for (Escala existente : existentes) {
            if (!nova.getDiaSemana().equals(existente.getDiaSemana())) {
                continue;
            }

            boolean sobreposicao = nova.getHoraInicio().isBefore(existente.getHoraFim())
                    && existente.getHoraInicio().isBefore(nova.getHoraFim());

            if (sobreposicao) {
                throw new ConflitoDeEscalaException(
                        "Conflito de horário com escala existente no mesmo dia da semana.");
            }

            if (!nova.getProfissionalVinculoId().equals(existente.getProfissionalVinculoId())) {
                boolean gapInsuficiente;
                if (!nova.getHoraFim().isAfter(existente.getHoraInicio())) {
                    gapInsuficiente = nova.getHoraFim().plusHours(1).isAfter(existente.getHoraInicio());
                } else {
                    gapInsuficiente = existente.getHoraFim().plusHours(1).isAfter(nova.getHoraInicio());
                }
                if (gapInsuficiente) {
                    throw new ConflitoDeEscalaException(
                            "Intervalo mínimo de 1 hora obrigatório entre escalas de estabelecimentos diferentes do mesmo profissional.");
                }
            }
        }
    }
}
