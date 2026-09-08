package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.*;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ItemNaoPermitidoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarEscalaUseCaseTest {

    @Mock private EscalaRepositoryPort escalaPort;
    @Mock private EscalaItemRepositoryPort escalaItemPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @InjectMocks private AtualizarEscalaUseCase useCase;

    private Escala escala() {
        return Escala.builder().id(1).profissionalVinculoId(10)
                .diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0))
                .estabelecimentoId(5).build();
    }

    private ProfissionalVinculo vinculo(int profissionalId) {
        return ProfissionalVinculo.builder().id(10).profissionalId(profissionalId).estabelecimentoId(5).build();
    }

    private void setupCommonMocks(int profissionalId) {
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala()));
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo(profissionalId)));
        when(vinculoServicoPort.listarPorProfissionalVinculoId(10)).thenReturn(
                List.of(ProfissionalVinculoServico.builder().id(1).profissionalVinculoId(10).servicoId(7).build()));
        when(profissionalVinculoPort.listarPorProfissionalId(profissionalId)).thenReturn(List.of(vinculo(profissionalId)));
        when(escalaPort.listarPorProfissionalVinculoId(10)).thenReturn(List.of());
        when(escalaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── isAdmin=true → skip access check ─────────────────────────────────────

    @Test
    void isAdmin_sucesso() {
        setupCommonMocks(30);

        Escala result = useCase.executar(1, DiaSemanaEnum.TERCA, LocalTime.of(9, 0), LocalTime.of(10, 0),
                List.of(7), "sub", true);

        assertThat(result).isNotNull();
    }

    // ── isAdmin=false, profissional matches → sucesso ─────────────────────────

    @Test
    void profissional_posseValida_sucesso() {
        setupCommonMocks(30);
        Usuario usuario = Usuario.builder().id(5).keycloakId("sub-prof").build();
        Profissional profissional = Profissional.builder().id(30).build();
        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(profissional));

        Escala result = useCase.executar(1, DiaSemanaEnum.TERCA, LocalTime.of(9, 0), LocalTime.of(10, 0),
                List.of(7), "sub-prof", false);

        assertThat(result).isNotNull();
    }

    // ── isAdmin=false, profissional NÃO é dono → AcessoNegado ─────────────────

    @Test
    void profissional_posseInvalida_lancaAcessoNegado() {
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala()));
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo(30)));
        Usuario usuario = Usuario.builder().id(5).keycloakId("sub-prof").build();
        Profissional profissional = Profissional.builder().id(99).build(); // different
        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(profissional));

        assertThatThrownBy(() -> useCase.executar(1, DiaSemanaEnum.TERCA, LocalTime.of(9, 0), LocalTime.of(10, 0),
                List.of(7), "sub-prof", false))
                .isInstanceOf(AcessoNegadoException.class);
    }

    // ── serviço não permitido → ItemNaoPermitidoException ────────────────────

    @Test
    void servicoNaoPermitido_lancaItemNaoPermitido() {
        when(escalaPort.buscarPorId(1)).thenReturn(Optional.of(escala()));
        when(profissionalVinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo(30)));
        when(vinculoServicoPort.listarPorProfissionalVinculoId(10)).thenReturn(List.of()); // empty → no services allowed

        assertThatThrownBy(() -> useCase.executar(1, DiaSemanaEnum.TERCA, LocalTime.of(9, 0), LocalTime.of(10, 0),
                List.of(7), "sub", true))
                .isInstanceOf(ItemNaoPermitidoException.class);
    }
}
