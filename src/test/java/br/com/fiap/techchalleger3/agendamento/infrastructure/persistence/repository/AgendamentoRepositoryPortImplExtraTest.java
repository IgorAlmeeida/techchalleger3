package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendamentoEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.AgendamentoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoRepositoryPortImplExtraTest {

    @Mock AgendamentoRepository agendamentoRepository;
    @Mock AgendaRepository agendaRepository;
    @Mock AgendamentoMapper mapper;
    @InjectMocks AgendamentoRepositoryPortImpl portImpl;

    @Test
    void buscarAgendadosPorClienteNaData_retornaListaMapeada() {
        LocalDate data = LocalDate.now();
        AgendamentoEntity entity = AgendamentoEntity.builder().codigo(1).codAgenda(10).build();
        Agendamento model = Agendamento.builder().id(1).agendaId(10).build();

        when(agendamentoRepository.findAgendadosPaisDoPacienteNaData(
                5, StatusAgendamentoEnum.AGENDADO, data))
                .thenReturn(List.of(entity));
        when(mapper.toModel(entity)).thenReturn(model);

        List<Agendamento> result = portImpl.buscarAgendadosPorClienteNaData(5, data);

        assertThat(result).containsExactly(model);
    }

    @Test
    void deletarTodosPorAgendaId_chamaDeletarFilhosEPais() {
        portImpl.deletarTodosPorAgendaId(99);

        verify(agendamentoRepository).deletarFilhosPorAgendaId(99);
        verify(agendamentoRepository).deletarTodosPorAgendaId(99);
    }
}
