package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfissionalUseCaseTest {

    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @InjectMocks private ProfissionalUseCase useCase;

    private Profissional prof(int id, int usuarioId) {
        return Profissional.builder().id(id).usuarioId(usuarioId).nome("Prof " + id).ativo(true).build();
    }

    @Test
    void deveBuscarPorId_admin_semValidarAcesso() {
        when(profissionalPort.buscarPorId(1)).thenReturn(Optional.of(prof(1, 10)));

        Profissional result = useCase.buscarPorId(1, "any", true);

        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void deveBuscarPorId_profissional_validaProprioAcesso() {
        Usuario usuario = Usuario.builder().id(10).build();
        when(profissionalPort.buscarPorId(1)).thenReturn(Optional.of(prof(1, 10)));
        when(usuarioPort.buscarPorCodKeycloak("kc-10")).thenReturn(Optional.of(usuario));

        Profissional result = useCase.buscarPorId(1, "kc-10", false);

        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void deveLancar_quandoProfissionalNaoAutorizado() {
        Usuario outro = Usuario.builder().id(99).build();
        when(profissionalPort.buscarPorId(1)).thenReturn(Optional.of(prof(1, 10)));
        when(usuarioPort.buscarPorCodKeycloak("kc-99")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> useCase.buscarPorId(1, "kc-99", false))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void deveLancar_quandoNaoEncontrado() {
        when(profissionalPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.buscarPorId(99, "x", true))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveAtualizar_admin() {
        Profissional profissional = prof(1, 10);
        when(profissionalPort.buscarPorId(1)).thenReturn(Optional.of(profissional));
        when(profissionalPort.salvar(any())).thenReturn(profissional);

        Profissional result = useCase.atualizar(1, "Novo Nome", List.of("Corte"), "Rua X", "x", true);

        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void deveInativar_semAgendas() {
        Profissional profissional = prof(1, 10);
        when(profissionalPort.buscarPorId(1)).thenReturn(Optional.of(profissional));
        when(profissionalVinculoPort.listarPorProfissionalId(1))
                .thenReturn(List.of(ProfissionalVinculo.builder().id(5).build()));
        when(agendaPort.existeAgendaFuturaPorVinculo(5)).thenReturn(false);

        useCase.inativar(1);

        assertThat(profissional.getAtivo()).isFalse();
    }

    @Test
    void deveListar_delegaParaPort() {
        when(profissionalPort.listarComFiltros(any(), any(), anyBoolean(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        useCase.listar("nome", "corte", false, org.springframework.data.domain.PageRequest.of(0, 10));

        verify(profissionalPort).listarComFiltros(any(), any(), anyBoolean(), any());
    }

    @Test
    void deveLancar_quandoInativarComAgendas() {
        Profissional profissional = prof(1, 10);
        when(profissionalPort.buscarPorId(1)).thenReturn(Optional.of(profissional));
        when(profissionalVinculoPort.listarPorProfissionalId(1))
                .thenReturn(List.of(ProfissionalVinculo.builder().id(5).build()));
        when(agendaPort.existeAgendaFuturaPorVinculo(5)).thenReturn(true);

        assertThatThrownBy(() -> useCase.inativar(1))
                .isInstanceOf(OperacaoInvalidaException.class);
    }
}
