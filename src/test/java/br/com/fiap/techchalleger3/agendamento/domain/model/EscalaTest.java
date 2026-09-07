package br.com.fiap.techchalleger3.agendamento.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class EscalaTest {

    private Escala escala(int vinculoId, DiaSemanaEnum dia, LocalTime inicio, LocalTime fim) {
        return Escala.builder()
                .profissionalVinculoId(vinculoId)
                .diaSemana(dia)
                .horaInicio(inicio)
                .horaFim(fim)
                .build();
    }

    @Test
    void deveConflitar_quandoMesmoDiaESobreposicaoParcial() {
        Escala a = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Escala b = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(11, 0), LocalTime.of(14, 0));
        assertThat(a.conflitaCom(b)).isTrue();
    }

    @Test
    void deveConflitar_quandoMesmoDiaESobreposicaoTotal() {
        Escala a = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Escala b = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        assertThat(a.conflitaCom(b)).isTrue();
    }

    @Test
    void naoDeveConflitar_quandoDiasDiferentes() {
        Escala a = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Escala b = escala(1, DiaSemanaEnum.TERCA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        assertThat(a.conflitaCom(b)).isFalse();
    }

    @Test
    void naoDeveConflitar_quandoVinculosDiferentes() {
        Escala a = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Escala b = escala(2, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        assertThat(a.conflitaCom(b)).isFalse();
    }

    @Test
    void naoDeveConflitar_quandoHorariosAdjacentes() {
        Escala a = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Escala b = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(12, 0), LocalTime.of(14, 0));
        assertThat(a.conflitaCom(b)).isFalse();
    }

    @Test
    void naoDeveConflitar_quandoHorariosDistintos() {
        Escala a = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Escala b = escala(1, DiaSemanaEnum.SEGUNDA, LocalTime.of(14, 0), LocalTime.of(17, 0));
        assertThat(a.conflitaCom(b)).isFalse();
    }
}
