package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.model.*;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EscalaDetalhadaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarEscalasUseCaseTest {

    @Mock private EscalaRepositoryPort escalaPort;
    @Mock private EscalaItemRepositoryPort escalaItemPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @InjectMocks private ListarEscalasUseCase useCase;

    private Escala escala() {
        return Escala.builder().id(1).profissionalVinculoId(10)
                .diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0)).build();
    }

    private void setupEnriquecimento(int vinculoId) {
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(vinculoId).profissionalId(30).estabelecimentoId(5).build();
        Profissional profissional = Profissional.builder().id(30).nome("Dr.X").build();
        Estabelecimento estabelecimento = Estabelecimento.builder().id(5).nome("Clinic").build();
        when(profissionalVinculoPort.buscarPorId(vinculoId)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.of(profissional));
        when(estabelecimentoPort.buscarPorId(5)).thenReturn(Optional.of(estabelecimento));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of());
    }

    // ── sem filtros → OperacaoInvalida ────────────────────────────────────────

    @Test
    void semFiltros_lancaOperacaoInvalida() {
        assertThatThrownBy(() -> useCase.executar(null, null, "sub", true))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("filtro");
    }

    // ── isAdmin=true → sucesso ────────────────────────────────────────────────

    @Test
    void isAdmin_comEstabelecimento_retornaEscalas() {
        when(escalaPort.listarPorFiltros(5, null)).thenReturn(List.of(escala()));
        setupEnriquecimento(10);

        List<EscalaDetalhadaResponse> result = useCase.executar(5, null, "sub", true);

        assertThat(result).hasSize(1);
    }

    // ── isAdmin=false, profissionalVinculoId=null → sem verificação de acesso ──

    @Test
    void profissional_semVinculoId_usaEstabelecimento() {
        when(escalaPort.listarPorFiltros(5, null)).thenReturn(List.of(escala()));
        setupEnriquecimento(10);

        List<EscalaDetalhadaResponse> result = useCase.executar(5, null, "sub-prof", false);

        assertThat(result).hasSize(1);
    }

    // ── isAdmin=false, profissionalVinculoId != null, profissional matches ─────

    @Test
    void profissional_comVinculoId_posseValida_sucesso() {
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(30).estabelecimentoId(5).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        Usuario usuario = Usuario.builder().id(1).keycloakId("sub-prof").build();
        Profissional profissional = Profissional.builder().id(30).build();
        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(1)).thenReturn(Optional.of(profissional));

        when(escalaPort.listarPorFiltros(null, 10)).thenReturn(List.of(escala()));
        // Override the buscarPorId stub for toDetalhada
        Profissional profissionalDetalhado = Profissional.builder().id(30).nome("Dr.X").build();
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.of(profissionalDetalhado));
        Estabelecimento estabelecimento = Estabelecimento.builder().id(5).nome("Clinic").build();
        when(estabelecimentoPort.buscarPorId(5)).thenReturn(Optional.of(estabelecimento));
        when(escalaItemPort.listarAtivosPorEscalaId(1)).thenReturn(List.of());

        List<EscalaDetalhadaResponse> result = useCase.executar(null, 10, "sub-prof", false);

        assertThat(result).hasSize(1);
    }

    // ── isAdmin=false, profissionalVinculoId != null, profissional NÃO é dono ──

    @Test
    void profissional_comVinculoId_posseInvalida_lancaAcessoNegado() {
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(30).estabelecimentoId(5).build();
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));
        Usuario usuario = Usuario.builder().id(1).keycloakId("sub-prof").build();
        Profissional profissional = Profissional.builder().id(99).build(); // different
        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(1)).thenReturn(Optional.of(profissional));

        assertThatThrownBy(() -> useCase.executar(null, 10, "sub-prof", false))
                .isInstanceOf(AcessoNegadoException.class);
    }
}
