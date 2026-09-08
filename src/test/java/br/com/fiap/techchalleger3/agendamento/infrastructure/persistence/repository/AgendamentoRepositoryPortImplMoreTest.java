package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendamentoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.AgendamentoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoRepositoryPortImplMoreTest {

    @Mock AgendamentoRepository agendamentoRepository;
    @Mock AgendaRepository agendaRepository;
    @Mock AgendamentoMapper mapper;
    @InjectMocks AgendamentoRepositoryPortImpl portImpl;

    private AgendamentoEntity agendamentoEntityAgendado(int codAgenda, LocalTime horaFim) {
        return AgendamentoEntity.builder()
                .codigo(1).codAgenda(codAgenda)
                .horaFim(horaFim).status(StatusAgendamentoEnum.AGENDADO).build();
    }

    @Test
    void buscarAgendadosPassados_agendaNaoEncontrada_retornaVazio() {
        AgendamentoEntity e = agendamentoEntityAgendado(99, LocalTime.of(8, 0));
        when(agendamentoRepository.findByStatus(StatusAgendamentoEnum.AGENDADO)).thenReturn(List.of(e));
        when(agendaRepository.findById(99)).thenReturn(Optional.empty());

        assertThat(portImpl.buscarAgendadosPassados()).isEmpty();
    }

    @Test
    void buscarAgendadosPassados_dataPassada_retornaAgendamento() {
        AgendamentoEntity e = agendamentoEntityAgendado(10, LocalTime.of(8, 0));
        Agendamento m = Agendamento.builder().id(1).status(StatusAgendamentoEnum.AGENDADO).build();
        AgendaEntity agenda = AgendaEntity.builder().codigo(10)
                .dataAgenda(LocalDate.now().minusDays(1)).build();

        when(agendamentoRepository.findByStatus(StatusAgendamentoEnum.AGENDADO)).thenReturn(List.of(e));
        when(agendaRepository.findById(10)).thenReturn(Optional.of(agenda));
        when(mapper.toModel(e)).thenReturn(m);

        assertThat(portImpl.buscarAgendadosPassados()).containsExactly(m);
    }

    @Test
    void buscarAgendadosPassados_dataHojeHoraPassada_retornaAgendamento() {
        LocalTime horaFimPassada = LocalTime.now().minusMinutes(5);
        AgendamentoEntity e = agendamentoEntityAgendado(10, horaFimPassada);
        Agendamento m = Agendamento.builder().id(1).status(StatusAgendamentoEnum.AGENDADO).build();
        AgendaEntity agenda = AgendaEntity.builder().codigo(10)
                .dataAgenda(LocalDate.now()).build();

        when(agendamentoRepository.findByStatus(StatusAgendamentoEnum.AGENDADO)).thenReturn(List.of(e));
        when(agendaRepository.findById(10)).thenReturn(Optional.of(agenda));
        when(mapper.toModel(e)).thenReturn(m);

        assertThat(portImpl.buscarAgendadosPassados()).containsExactly(m);
    }

    @Test
    void buscarAgendadosPassados_dataHojeHoraFutura_retornaVazio() {
        LocalTime horaFimFutura = LocalTime.now().plusHours(1);
        AgendamentoEntity e = agendamentoEntityAgendado(10, horaFimFutura);
        AgendaEntity agenda = AgendaEntity.builder().codigo(10)
                .dataAgenda(LocalDate.now()).build();

        when(agendamentoRepository.findByStatus(StatusAgendamentoEnum.AGENDADO)).thenReturn(List.of(e));
        when(agendaRepository.findById(10)).thenReturn(Optional.of(agenda));

        assertThat(portImpl.buscarAgendadosPassados()).isEmpty();
    }

    @Test
    void buscarAgendadosPassados_dataFutura_retornaVazio() {
        AgendamentoEntity e = agendamentoEntityAgendado(10, LocalTime.of(20, 0));
        AgendaEntity agenda = AgendaEntity.builder().codigo(10)
                .dataAgenda(LocalDate.now().plusDays(1)).build();

        when(agendamentoRepository.findByStatus(StatusAgendamentoEnum.AGENDADO)).thenReturn(List.of(e));
        when(agendaRepository.findById(10)).thenReturn(Optional.of(agenda));

        assertThat(portImpl.buscarAgendadosPassados()).isEmpty();
    }

    @Test
    void buscarPaisPorClienteId() {
        AgendamentoEntity e = AgendamentoEntity.builder().codigo(1).codAgenda(10).build();
        Agendamento m = Agendamento.builder().id(1).agendaId(10).build();
        when(agendamentoRepository.buscarPaisDoPaciente(anyInt(), any(), any(), any()))
                .thenReturn(List.of(e));
        when(mapper.toModel(e)).thenReturn(m);

        assertThat(portImpl.buscarPaisPorClienteId(10, List.of(StatusAgendamentoEnum.AGENDADO), null, null))
                .containsExactly(m);
    }

    @Test
    void existeAgendadoPorClienteVinculoServico() {
        when(agendamentoRepository.existeAgendadoPorClienteVinculoServico(
                10, 1, 5, StatusAgendamentoEnum.AGENDADO)).thenReturn(true);

        assertThat(portImpl.existeAgendadoPorClienteVinculoServico(10, 1, 5)).isTrue();
    }

    @Test
    void buscarPaisPorProfissionalVinculoIds_comIds() {
        AgendamentoEntity e = AgendamentoEntity.builder().codigo(1).codAgenda(10).build();
        Agendamento m = Agendamento.builder().id(1).agendaId(10).build();
        when(agendamentoRepository.buscarPaisDosProfissionaisVinculos(any(), any(), any(), any()))
                .thenReturn(List.of(e));
        when(mapper.toModel(e)).thenReturn(m);

        assertThat(portImpl.buscarPaisPorProfissionalVinculoIds(
                List.of(1, 2), List.of(StatusAgendamentoEnum.AGENDADO), null, null))
                .containsExactly(m);
    }
}
