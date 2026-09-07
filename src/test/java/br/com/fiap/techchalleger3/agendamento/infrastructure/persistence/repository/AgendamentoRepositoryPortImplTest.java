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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoRepositoryPortImplTest {

    @Mock AgendamentoRepository agendamentoRepository;
    @Mock AgendaRepository agendaRepository;
    @Mock AgendamentoMapper mapper;
    @InjectMocks AgendamentoRepositoryPortImpl portImpl;

    @Test
    void buscarPorId_encontrado() {
        AgendamentoEntity e = AgendamentoEntity.builder().codigo(1).codAgenda(10).build();
        Agendamento m = Agendamento.builder().id(1).agendaId(10).build();
        when(agendamentoRepository.findById(1)).thenReturn(Optional.of(e));
        when(mapper.toModel(e)).thenReturn(m);

        assertThat(portImpl.buscarPorId(1)).contains(m);
    }

    @Test
    void listarPorAgendaId() {
        AgendamentoEntity e = AgendamentoEntity.builder().codigo(1).codAgenda(5).build();
        Agendamento m = Agendamento.builder().id(1).agendaId(5).build();
        when(agendamentoRepository.findByCodAgenda(5)).thenReturn(List.of(e));
        when(mapper.toModel(e)).thenReturn(m);

        assertThat(portImpl.listarPorAgendaId(5)).containsExactly(m);
    }

    @Test
    void buscarDisponiveisPorVinculo_agendasVazias() {
        when(agendaRepository.findByCodProfissionalVinculo(1)).thenReturn(List.of());

        assertThat(portImpl.buscarDisponiveisPorVinculo(1)).isEmpty();
    }

    @Test
    void buscarDisponiveisPorVinculo_comAgendas() {
        AgendaEntity agenda = AgendaEntity.builder().codigo(10).codProfissionalVinculo(1)
                .dataAgenda(LocalDate.now().plusDays(1)).build();
        AgendamentoEntity ae = AgendamentoEntity.builder().codigo(1).codAgenda(10).build();
        Agendamento m = Agendamento.builder().id(1).agendaId(10).status(StatusAgendamentoEnum.DISPONIVEL).build();

        when(agendaRepository.findByCodProfissionalVinculo(1)).thenReturn(List.of(agenda));
        when(agendamentoRepository.findByCodAgendaInAndStatus(List.of(10), StatusAgendamentoEnum.DISPONIVEL))
                .thenReturn(List.of(ae));
        when(mapper.toModel(ae)).thenReturn(m);

        assertThat(portImpl.buscarDisponiveisPorVinculo(1)).containsExactly(m);
    }

    @Test
    void buscarFilhosPorPaiId() {
        AgendamentoEntity e = AgendamentoEntity.builder().codigo(2).codAgendamentoPai(1).build();
        Agendamento m = Agendamento.builder().id(2).agendamentoPaiId(1).build();
        when(agendamentoRepository.findByCodAgendamentoPai(1)).thenReturn(List.of(e));
        when(mapper.toModel(e)).thenReturn(m);

        assertThat(portImpl.buscarFilhosPorPaiId(1)).containsExactly(m);
    }

    @Test
    void buscarPaisPorProfissionalVinculoIds_vazio() {
        assertThat(portImpl.buscarPaisPorProfissionalVinculoIds(List.of(), null, null, null)).isEmpty();
        assertThat(portImpl.buscarPaisPorProfissionalVinculoIds(null, null, null, null)).isEmpty();
    }

    @Test
    void salvar() {
        Agendamento m = Agendamento.builder().id(1).agendaId(10).status(StatusAgendamentoEnum.AGENDADO).build();
        AgendamentoEntity e = AgendamentoEntity.builder().codigo(1).codAgenda(10).build();
        when(mapper.toEntity(m)).thenReturn(e);
        when(agendamentoRepository.save(e)).thenReturn(e);
        when(mapper.toModel(e)).thenReturn(m);

        assertThat(portImpl.salvar(m)).isEqualTo(m);
    }
}
