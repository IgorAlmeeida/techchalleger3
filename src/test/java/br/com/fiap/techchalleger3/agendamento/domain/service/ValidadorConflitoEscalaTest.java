package br.com.fiap.techchalleger3.agendamento.domain.service;

import br.com.fiap.techchalleger3.agendamento.domain.exception.ConflitoDeEscalaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ValidadorConflitoEscalaTest {

    private ValidadorConflitoEscala validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorConflitoEscala();
    }

    private Escala escala(int vinculoId, DiaSemanaEnum dia, LocalTime inicio, LocalTime fim) {
        return Escala.builder()
                .profissionalVinculoId(vinculoId)
                .diaSemana(dia)
                .horaInicio(inicio)
                .horaFim(fim)
                .build();
    }

    @Test
    void deveLancarExcecao_quandoOverlapNoMesmoDia() {
        Escala nova = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Escala existente = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(11, 0), LocalTime.of(14, 0));

        assertThatThrownBy(() -> validador.validar(nova, List.of(existente)))
                .isInstanceOf(ConflitoDeEscalaException.class);
    }

    @Test
    void deveLancarExcecao_quandoGapInsuficiente_diferentesEstabelecimentos() {
        // nova termina 11:00, existente começa 11:30 → gap = 30min < 1h
        Escala nova = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(11, 0));
        Escala existente = escala(2, DiaSemanaEnum.SEGUNDA, LocalTime.of(11, 30), LocalTime.of(14, 0));

        assertThatThrownBy(() -> validador.validar(nova, List.of(existente)))
                .isInstanceOf(ConflitoDeEscalaException.class);
    }

    @Test
    void naoDeveLancar_quandoGapSuficiente_diferentesEstabelecimentos() {
        // nova termina 10:00, existente começa 11:30 → gap = 1h30 >= 1h
        Escala nova = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(8, 0), LocalTime.of(10, 0));
        Escala existente = escala(2, DiaSemanaEnum.SEGUNDA, LocalTime.of(11, 30), LocalTime.of(14, 0));

        assertThatNoException().isThrownBy(() -> validador.validar(nova, List.of(existente)));
    }

    @Test
    void naoDeveLancar_quandoDiasDiferentes() {
        Escala nova = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Escala existente = escala(1, DiaSemanaEnum.TERCA, LocalTime.of(9, 0), LocalTime.of(12, 0));

        assertThatNoException().isThrownBy(() -> validador.validar(nova, List.of(existente)));
    }
}
