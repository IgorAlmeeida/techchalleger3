package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerarAgendaUseCaseTest {

    @Mock private EscalaRepositoryPort escalaPort;
    @Mock private EscalaItemRepositoryPort escalaItemPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private AgendaItemRepositoryPort agendaItemPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @InjectMocks private GerarAgendaUseCase useCase;

    private Escala escalaMondayWindow() {
        return Escala.builder().id(1).profissionalVinculoId(10)
                .diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 10))
                .estabelecimentoId(5).build();
    }

    private EscalaItem item() {
        return EscalaItem.builder().id(1).escalaId(1).servicoId(7).ativa(true).build();
    }

    @Test
    void escalaInativa_semItens_lancaOperacaoInvalida() {
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escalaMondayWindow()));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.executar(1, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 11), "sub", true))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("itens ativos");
    }

    @Test
    void nenhumaDiaNoIntervalo_lancaOperacaoInvalida() {
        Escala escala = escalaMondayWindow();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of(item()));
        // Interval 2026-10-06 (Tue) to 2026-10-07 (Wed) — no Monday
        assertThatThrownBy(() -> useCase.executar(1, LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 7), "sub", true))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("dia da semana");
    }

    @Test
    void isAdmin_geraAgenda_semValidarPosse() {
        Escala escala = escalaMondayWindow();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of(item()));
        when(agendaPort.existeAgendaPorVinculoEData(any(), any())).thenReturn(false);

        Agenda savedAgenda = Agenda.builder().id(10).profissionalVinculoId(10)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 10)).build();
        when(agendaPort.salvar(any())).thenReturn(savedAgenda);
        when(agendaItemPort.salvar(any())).thenReturn(null);
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        // 2026-10-05 is a Monday
        List<Agenda> result = useCase.executar(1, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5), "sub-admin", true);

        assertThat(result).hasSize(1);
    }

    @Test
    void isProfissional_validarPosse_sucesso() {
        Escala escala = escalaMondayWindow();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of(item()));

        Usuario usuario = Usuario.builder().id(5).keycloakId("sub-prof").build();
        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        Profissional profissional = Profissional.builder().id(30).build();
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(profissional));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(30).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));

        when(agendaPort.existeAgendaPorVinculoEData(any(), any())).thenReturn(false);
        Agenda savedAgenda = Agenda.builder().id(10).profissionalVinculoId(10)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 10)).build();
        when(agendaPort.salvar(any())).thenReturn(savedAgenda);
        when(agendaItemPort.salvar(any())).thenReturn(null);
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Agenda> result = useCase.executar(1, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5), "sub-prof", false);

        assertThat(result).hasSize(1);
    }

    @Test
    void isProfissional_vinculoNaoAutorizado_lancaAcessoNegado() {
        Escala escala = escalaMondayWindow();
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala));

        Usuario usuario = Usuario.builder().id(5).keycloakId("sub-prof").build();
        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        Profissional profissional = Profissional.builder().id(99).build(); // different profissional
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(profissional));
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(30).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));

        assertThatThrownBy(() -> useCase.executar(1, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5), "sub-prof", false))
                .isInstanceOf(AcessoNegadoException.class);
    }
}
