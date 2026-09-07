package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EscalaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EscalaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ItemNaoPermitidoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarEscalaUseCaseTest {

    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    @Mock private EscalaRepositoryPort escalaPort;
    @Mock private EscalaItemRepositoryPort escalaItemPort;
    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;

    @InjectMocks private CriarEscalaUseCase useCase;

    @Test
    void deveLancarExcecao_quandoVinculoNaoEncontrado() {
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(
                1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0),
                List.of(1), "sub", true))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecao_quandoVinculoInativo() {
        ProfissionalVinculo vinculoInativo = ProfissionalVinculo.builder()
                .id(1).profissionalId(1).estabelecimentoId(1)
                .dataFim(java.time.LocalDate.now())
                .build();
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculoInativo));

        assertThatThrownBy(() -> useCase.executar(
                1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0),
                List.of(1), "sub", true))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecao_quandoServicoNaoPermitido() {
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder()
                .id(1).profissionalId(1).estabelecimentoId(1).build();
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo));
        when(vinculoServicoPort.listarPorProfissionalVinculoId(1)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.executar(
                1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0),
                List.of(99), "sub", true))
                .isInstanceOf(ItemNaoPermitidoException.class);
    }

    @Test
    void deveCriarEscala_quandoAdminEDadosValidos() {
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder()
                .id(1).profissionalId(1).estabelecimentoId(1).build();
        ProfissionalVinculoServico pvServico = ProfissionalVinculoServico.builder()
                .profissionalVinculoId(1).servicoId(1).build();
        Escala escalaSalva = Escala.builder()
                .id(10).profissionalVinculoId(1).estabelecimentoId(1)
                .diaSemana(DiaSemanaEnum.SEGUNDA)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(12, 0)).build();

        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo));
        when(vinculoServicoPort.listarPorProfissionalVinculoId(1)).thenReturn(List.of(pvServico));
        when(profissionalVinculoPort.listarPorProfissionalId(1)).thenReturn(List.of(vinculo));
        when(escalaPort.listarPorProfissionalVinculoId(1)).thenReturn(List.of());
        when(escalaPort.salvar(any(Escala.class))).thenReturn(escalaSalva);
        when(escalaItemPort.salvar(any(EscalaItem.class))).thenReturn(EscalaItem.builder().id(1).build());

        Escala resultado = useCase.executar(
                1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0),
                List.of(1), "sub", true);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10);
        verify(escalaPort).salvar(any(Escala.class));
        verify(escalaItemPort, times(1)).salvar(any(EscalaItem.class));
    }

    @Test
    void deveLancarAcessoNegado_quandoProfissionalNaoEDono() {
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder()
                .id(1).profissionalId(10).estabelecimentoId(1).build();
        when(profissionalVinculoPort.buscarPorId(1)).thenReturn(Optional.of(vinculo));
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(5).build()));
        when(profissionalPort.buscarPorUsuarioId(5)).thenReturn(Optional.of(Profissional.builder().id(99).build()));

        assertThatThrownBy(() -> useCase.executar(
                1, DiaSemanaEnum.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0),
                List.of(), "kc", false))
                .isInstanceOf(AcessoNegadoException.class);
    }
}
