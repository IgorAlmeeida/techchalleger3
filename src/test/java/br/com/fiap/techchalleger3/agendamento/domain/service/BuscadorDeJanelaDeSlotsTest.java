package br.com.fiap.techchalleger3.agendamento.domain.service;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BuscadorDeJanelaDeSlotsTest {

    private Agendamento slot(int agendaId, LocalTime inicio, LocalTime fim) {
        return Agendamento.builder()
                .id(agendaId * 100 + inicio.getMinute())
                .agendaId(agendaId)
                .horaInicio(inicio)
                .horaFim(fim)
                .build();
    }

    @Test
    void agrupar_ordenaEAgrupaAgenda() {
        Agendamento s1 = slot(1, LocalTime.of(9, 0), LocalTime.of(9, 5));
        Agendamento s2 = slot(1, LocalTime.of(9, 10), LocalTime.of(9, 15));
        Agendamento s3 = slot(2, LocalTime.of(10, 0), LocalTime.of(10, 5));

        Map<Integer, List<Agendamento>> resultado = BuscadorDeJanelaDeSlots.agrupar(List.of(s3, s1, s2));

        assertThat(resultado).containsKeys(1, 2);
        assertThat(resultado.get(1).get(0).getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void buscarPrimeiraJanela_encontraJanelaConsecutiva() {
        Agendamento s1 = slot(1, LocalTime.of(9, 0), LocalTime.of(9, 5));
        Agendamento s2 = slot(1, LocalTime.of(9, 5), LocalTime.of(9, 10));
        Agendamento s3 = slot(1, LocalTime.of(9, 10), LocalTime.of(9, 15));

        Map<Integer, List<Agendamento>> porAgenda = new LinkedHashMap<>();
        porAgenda.put(1, List.of(s1, s2, s3));

        Optional<List<Agendamento>> janela = BuscadorDeJanelaDeSlots.buscarPrimeiraJanela(porAgenda, 2);

        assertThat(janela).isPresent();
        assertThat(janela.get()).hasSize(2);
        assertThat(janela.get().get(0).getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void buscarPrimeiraJanela_retornaVazioSeNaoHaJanela() {
        Agendamento s1 = slot(1, LocalTime.of(9, 0), LocalTime.of(9, 5));
        Agendamento s2 = slot(1, LocalTime.of(9, 10), LocalTime.of(9, 15));

        Map<Integer, List<Agendamento>> porAgenda = new LinkedHashMap<>();
        porAgenda.put(1, List.of(s1, s2));

        Optional<List<Agendamento>> janela = BuscadorDeJanelaDeSlots.buscarPrimeiraJanela(porAgenda, 2);

        assertThat(janela).isEmpty();
    }

    @Test
    void buscarIniciosDeJanelas_retornaIniciosCandidatos() {
        Agendamento s1 = slot(1, LocalTime.of(9, 0), LocalTime.of(9, 5));
        Agendamento s2 = slot(1, LocalTime.of(9, 5), LocalTime.of(9, 10));
        Agendamento s3 = slot(1, LocalTime.of(9, 10), LocalTime.of(9, 15));

        Map<Integer, List<Agendamento>> porAgenda = new LinkedHashMap<>();
        porAgenda.put(1, List.of(s1, s2, s3));

        List<Agendamento> inicios = BuscadorDeJanelaDeSlots.buscarIniciosDeJanelas(porAgenda, 2);

        assertThat(inicios).hasSize(2);
        assertThat(inicios.get(0).getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void buscarPrimeiraJanela_mapVazio_retornaVazio() {
        Optional<List<Agendamento>> janela = BuscadorDeJanelaDeSlots.buscarPrimeiraJanela(Map.of(), 2);
        assertThat(janela).isEmpty();
    }
}
