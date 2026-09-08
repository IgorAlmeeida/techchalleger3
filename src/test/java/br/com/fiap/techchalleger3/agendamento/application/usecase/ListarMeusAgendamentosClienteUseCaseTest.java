package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.model.*;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarMeusAgendamentosClienteUseCaseTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @InjectMocks private ListarMeusAgendamentosClienteUseCase useCase;

    private void setupBase(int clienteId, Agendamento agendamento) {
        Usuario usuario = Usuario.builder().id(1).keycloakId("sub").build();
        Cliente cliente = Cliente.builder().id(clienteId).email("c@x.com").build();
        when(usuarioPort.buscarPorCodKeycloak("sub")).thenReturn(Optional.of(usuario));
        when(clientePort.buscarPorUsuarioId(1)).thenReturn(Optional.of(cliente));
        when(agendamentoPort.buscarPaisPorClienteId(org.mockito.ArgumentMatchers.eq(clienteId),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull()))
                .thenReturn(List.of(agendamento));

        Agenda agenda = Agenda.builder().id(agendamento.getAgendaId()).profissionalVinculoId(10)
                .dataAgenda(LocalDate.now()).build();
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(30).estabelecimentoId(5).build();
        Profissional profissional = Profissional.builder().id(30).nome("Dr.X").build();
        Estabelecimento estabelecimento = Estabelecimento.builder().id(5).nome("Clinic").build();

        when(agendaPort.buscarPorId(agendamento.getAgendaId())).thenReturn(Optional.of(agenda));
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.of(profissional));
        when(estabelecimentoPort.buscarPorId(5)).thenReturn(Optional.of(estabelecimento));
    }

    // ── statuses == null → usa STATUS_PADRAO ─────────────────────────────────

    @Test
    void statusNull_usaStatusPadrao() {
        Agendamento a = Agendamento.builder().id(1).agendaId(20).status(StatusAgendamentoEnum.AGENDADO)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5)).servicoId(null).build();
        setupBase(10, a);

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                useCase.executar("sub", null, null, null);

        assertThat(result).hasSize(1);
    }

    // ── statuses não null mas com servicoId null ──────────────────────────────

    @Test
    void comStatus_servicoNull_retornaSemServico() {
        Agendamento a = Agendamento.builder().id(1).agendaId(20).status(StatusAgendamentoEnum.AGENDADO)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5)).servicoId(null).build();
        setupBase(10, a);

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                useCase.executar("sub", List.of(StatusAgendamentoEnum.AGENDADO), null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServico()).isNull();
    }

    // ── servicoId não null → busca serviço ────────────────────────────────────

    @Test
    void comServicoId_retornaServicoResumo() {
        Agendamento a = Agendamento.builder().id(1).agendaId(20).status(StatusAgendamentoEnum.AGENDADO)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 5)).servicoId(7).build();
        setupBase(10, a);
        when(servicoPort.buscarPorId(7)).thenReturn(Optional.of(
                Servico.builder().id(7).nome("Corte").duracaoMinutos(30).build()));

        List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> result =
                useCase.executar("sub", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServico()).isNotNull();
    }
}
