package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoUseCaseTest {

    // Shared mocks
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaItemRepositoryPort agendaItemPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ClienteRepositoryPort clientePort;

    // ListarHorariosDisponiveisUseCase
    @InjectMocks private ListarHorariosDisponiveisUseCase listarHorarios;

    // ListarMeusAgendamentosClienteUseCase
    @InjectMocks private ListarMeusAgendamentosClienteUseCase listarMeusAgendamentos;

    // ListarAgendamentosProfissionalUseCase
    @InjectMocks private ListarAgendamentosProfissionalUseCase listarAgendamentosProfissional;

    @Test
    void horarios_lancaQuandoVinculoNulo() {
        assertThatThrownBy(() -> listarHorarios.executar(null, 1))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void horarios_lancaQuandoServicoNulo() {
        assertThatThrownBy(() -> listarHorarios.executar(1, null))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void horarios_retornaJanelasDisponiveis() {
        Servico servico = Servico.builder().id(5).nome("Corte").duracaoMinutos(10).preco(BigDecimal.TEN).build();
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(servico));

        // Two consecutive 5-min slots in same agenda
        Agendamento s1 = Agendamento.builder().id(1).agendaId(10).horaInicio(LocalTime.of(9,0)).horaFim(LocalTime.of(9,5)).status(StatusAgendamentoEnum.DISPONIVEL).build();
        Agendamento s2 = Agendamento.builder().id(2).agendaId(10).horaInicio(LocalTime.of(9,5)).horaFim(LocalTime.of(9,10)).status(StatusAgendamentoEnum.DISPONIVEL).build();
        when(agendamentoPort.buscarDisponiveisPorVinculo(1)).thenReturn(List.of(s1, s2));
        when(agendaItemPort.existePorAgendaIdsEServico(List.of(10), 5)).thenReturn(true);

        Agenda agenda = Agenda.builder().id(10).profissionalVinculoId(20).dataAgenda(LocalDate.now()).build();
        when(agendaPort.buscarPorId(10)).thenReturn(Optional.of(agenda));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).estabelecimentoId(40).build();
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.of(Profissional.builder().id(30).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(40)).thenReturn(Optional.of(Estabelecimento.builder().id(40).nome("Studio").build()));

        List<ListarHorariosDisponiveisUseCase.HorarioDisponivel> result = listarHorarios.executar(1, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void meusAgendamentos_lancaQuandoUsuarioNaoEncontrado() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listarMeusAgendamentos.executar("kc", null, null, null))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void meusAgendamentos_retornaAgendamentosEnriquecidos() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(1).build()));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(Cliente.builder().id(10).build()));

        Agendamento ag = Agendamento.builder().id(5).agendaId(100).horaInicio(LocalTime.of(9,0))
                .horaFim(LocalTime.of(9,30)).status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(false)
                .servicoId(5).build();
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(Servico.builder().id(5).nome("Corte").duracaoMinutos(30).preco(java.math.BigDecimal.TEN).build()));
        when(agendamentoPort.buscarPaisPorClienteId(any(), any(), any(), any())).thenReturn(List.of(ag));

        Agenda agenda = Agenda.builder().id(100).profissionalVinculoId(20).dataAgenda(LocalDate.now()).build();
        when(agendaPort.buscarPorId(100)).thenReturn(Optional.of(agenda));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).estabelecimentoId(40).build();
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.of(Profissional.builder().id(30).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(40)).thenReturn(Optional.of(Estabelecimento.builder().id(40).nome("Studio").build()));

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                listarMeusAgendamentos.executar("kc", null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void agendamentosProfissional_admin_lancaQuandoSemVinculoId() {
        assertThatThrownBy(() -> listarAgendamentosProfissional.executar("x", null, null, null, null, true))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void agendamentosProfissional_profissional_retornaAgendamentos() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(1).build()));
        when(profissionalPort.buscarPorUsuarioId(1)).thenReturn(Optional.of(Profissional.builder().id(10).build()));
        when(profissionalVinculoPort.listarPorProfissionalId(10)).thenReturn(List.of(
                ProfissionalVinculo.builder().id(50).build()
        ));

        Agendamento ag = Agendamento.builder().id(7).agendaId(100).horaInicio(LocalTime.of(9,0))
                .horaFim(LocalTime.of(9,30)).status(StatusAgendamentoEnum.AGENDADO).clienteId(20).servicoId(5)
                .presencaConfirmada(false).build();
        when(agendamentoPort.buscarPaisPorProfissionalVinculoIds(any(), any(), any(), any())).thenReturn(List.of(ag));

        Agenda agenda = Agenda.builder().id(100).profissionalVinculoId(50).dataAgenda(LocalDate.now()).build();
        when(agendaPort.buscarPorId(100)).thenReturn(Optional.of(agenda));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(50).profissionalId(10).estabelecimentoId(30).build();
        when(profissionalVinculoPort.buscarPorId(50)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(10)).thenReturn(Optional.of(Profissional.builder().id(10).nome("Dr.").build()));
        when(estabelecimentoPort.buscarPorId(30)).thenReturn(Optional.of(Estabelecimento.builder().id(30).nome("Studio").build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(Servico.builder().id(5).nome("Corte").duracaoMinutos(30).preco(java.math.BigDecimal.TEN).build()));
        when(clientePort.buscarPorId(20)).thenReturn(Optional.of(Cliente.builder().id(20).nome("Ana").build()));

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                listarAgendamentosProfissional.executar("kc", null, null, null, null, false);

        assertThat(result).hasSize(1);
    }

    @Test
    void agendamentosProfissional_admin_comVinculoIdFiltro_retornaVazio() {
        when(agendamentoPort.buscarPaisPorProfissionalVinculoIds(any(), any(), any(), any())).thenReturn(List.of());

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                listarAgendamentosProfissional.executar("x", 50, null, null, null, true);

        assertThat(result).isEmpty();
    }
}
