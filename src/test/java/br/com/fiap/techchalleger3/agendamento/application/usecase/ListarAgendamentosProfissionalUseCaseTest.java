package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.*;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ClienteResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarAgendamentosProfissionalUseCaseTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @InjectMocks private ListarAgendamentosProfissionalUseCase useCase;

    private Agendamento agendamento(Integer clienteId, Integer servicoId) {
        return Agendamento.builder().id(1).agendaId(10)
                .status(StatusAgendamentoEnum.AGENDADO)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5))
                .clienteId(clienteId).servicoId(servicoId).build();
    }

    private void setupEnriquecer(int agendaId, int vinculoId, int profissionalId, int estabelecimentoId) {
        Agenda agenda = Agenda.builder().id(agendaId).profissionalVinculoId(vinculoId)
                .dataAgenda(LocalDate.now()).build();
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(vinculoId)
                .profissionalId(profissionalId).estabelecimentoId(estabelecimentoId).build();
        Profissional profissional = Profissional.builder().id(profissionalId).nome("Dr. X").build();
        Estabelecimento estabelecimento = Estabelecimento.builder().id(estabelecimentoId).nome("Clínica").build();

        when(agendaPort.buscarPorId(agendaId)).thenReturn(Optional.of(agenda));
        when(profissionalVinculoPort.buscarPorId(vinculoId)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(profissionalId)).thenReturn(Optional.of(profissional));
        when(estabelecimentoPort.buscarPorId(estabelecimentoId)).thenReturn(Optional.of(estabelecimento));
    }

    // ── isAdmin=true, com filtro → usa filtro ────────────────────────────────────

    @Test
    void isAdmin_comFiltro_retornaAgendamentos() {
        Agendamento a = agendamento(null, null);
        when(agendamentoPort.buscarPaisPorProfissionalVinculoIds(List.of(5), List.of(StatusAgendamentoEnum.AGENDADO, StatusAgendamentoEnum.RESERVADO), null, null))
                .thenReturn(List.of(a));
        setupEnriquecer(10, 5, 30, 7);

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                useCase.executar("sub", 5, List.of(StatusAgendamentoEnum.AGENDADO, StatusAgendamentoEnum.RESERVADO), null, null, true);

        assertThat(result).hasSize(1);
    }

    // ── isAdmin=true, sem filtro → OperacaoInvalida ───────────────────────────────

    @Test
    void isAdmin_semFiltro_lancaOperacaoInvalida() {
        assertThatThrownBy(() -> useCase.executar("sub", null, null, null, null, true))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("profissionalVinculoId");
    }

    // ── isAdmin=false, com filtro → usa filtro ────────────────────────────────────

    @Test
    void profissional_comFiltro_retornaAgendamentos() {
        Usuario usuario = Usuario.builder().id(1).keycloakId("sub-prof").build();
        Profissional profissional = Profissional.builder().id(30).build();
        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(1)).thenReturn(Optional.of(profissional));

        Agendamento a = agendamento(null, null);
        when(agendamentoPort.buscarPaisPorProfissionalVinculoIds(List.of(5), List.of(StatusAgendamentoEnum.AGENDADO, StatusAgendamentoEnum.RESERVADO), null, null))
                .thenReturn(List.of(a));
        setupEnriquecer(10, 5, 30, 7);

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                useCase.executar("sub-prof", 5, null, null, null, false);

        assertThat(result).hasSize(1);
    }

    // ── isAdmin=false, sem filtro → usa todos os vínculos ────────────────────────

    @Test
    void profissional_semFiltro_usaTodosVinculos() {
        Usuario usuario = Usuario.builder().id(1).keycloakId("sub-prof").build();
        Profissional profissional = Profissional.builder().id(30).build();
        ProfissionalVinculo v1 = ProfissionalVinculo.builder().id(5).profissionalId(30).build();
        ProfissionalVinculo v2 = ProfissionalVinculo.builder().id(6).profissionalId(30).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(1)).thenReturn(Optional.of(profissional));
        when(profissionalVinculoPort.listarPorProfissionalId(30)).thenReturn(List.of(v1, v2));
        when(agendamentoPort.buscarPaisPorProfissionalVinculoIds(List.of(5, 6), List.of(StatusAgendamentoEnum.AGENDADO, StatusAgendamentoEnum.RESERVADO), null, null))
                .thenReturn(List.of());

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                useCase.executar("sub-prof", null, null, null, null, false);

        assertThat(result).isEmpty();
    }

    // ── statuses null → usa STATUS_PADRAO ─────────────────────────────────────────
    // Covered by tests above (statuses=null path uses STATUS_PADRAO)

    // ── enriquecer: servicoId not null → busca serviço ───────────────────────────

    @Test
    void enriquecer_comServicoId_retornaServicoResumo() {
        Agendamento a = agendamento(null, 7);
        when(agendamentoPort.buscarPaisPorProfissionalVinculoIds(List.of(5), List.of(StatusAgendamentoEnum.AGENDADO, StatusAgendamentoEnum.RESERVADO), null, null))
                .thenReturn(List.of(a));
        setupEnriquecer(10, 5, 30, 7);
        when(servicoPort.buscarPorId(7)).thenReturn(Optional.of(
                Servico.builder().id(7).nome("Corte").duracaoMinutos(30).build()));

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                useCase.executar("sub", 5, null, null, null, true);

        assertThat(result.get(0).getServico()).isNotNull();
        assertThat(result.get(0).getServico()).isInstanceOf(ServicoResumo.class);
    }

    // ── enriquecer: clienteId not null → busca cliente ───────────────────────────

    @Test
    void enriquecer_comClienteId_retornaClienteResumo() {
        Agendamento a = agendamento(100, null);
        when(agendamentoPort.buscarPaisPorProfissionalVinculoIds(List.of(5), List.of(StatusAgendamentoEnum.AGENDADO, StatusAgendamentoEnum.RESERVADO), null, null))
                .thenReturn(List.of(a));
        setupEnriquecer(10, 5, 30, 7);
        when(clientePort.buscarPorId(100)).thenReturn(Optional.of(
                Cliente.builder().id(100).nome("Maria").build()));

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                useCase.executar("sub", 5, null, null, null, true);

        assertThat(result.get(0).getCliente()).isNotNull();
        assertThat(result.get(0).getCliente()).isInstanceOf(ClienteResumo.class);
    }
}
