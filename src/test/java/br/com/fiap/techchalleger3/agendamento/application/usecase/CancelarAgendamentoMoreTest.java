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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarAgendamentoMoreTest {

    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ClienteRepositoryPort clientePort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private AgendaRepositoryPort agendaPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @Mock private EmailSenderPort emailSenderPort;
    @InjectMocks private CancelarAgendamentoUseCase useCase;

    private Agenda agenda() {
        return Agenda.builder().id(7).dataAgenda(LocalDate.now().plusDays(1))
                .profissionalVinculoId(20).build();
    }

    private Agendamento agendamentoPai(int clienteId) {
        return Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(null)
                .clienteId(clienteId).servicoId(5)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0))
                .status(StatusAgendamentoEnum.AGENDADO).presencaConfirmada(false).build();
    }

    @Test
    void cancelarComoProfissional_sucesso() {
        Usuario usuario = Usuario.builder().id(20).keycloakId("sub-prof").role(RoleEnum.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(30).nome("Dr.").build();
        Agendamento agendamento = agendamentoPai(100);
        Agenda ag = agenda();
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).estabelecimentoId(40).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(20)).thenReturn(Optional.of(profissional));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clientePort.buscarPorId(100)).thenReturn(Optional.of(
                Cliente.builder().id(100).email("c@x.com").build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar(1, "sub-prof");

        assertThat(result.getStatus()).isEqualTo(StatusAgendamentoEnum.CANCELADO);
        verify(emailSenderPort).enviar(any());
    }

    @Test
    void cancelarComoProfissional_vinculoNaoAutorizado_lancaAcessoNegado() {
        Usuario usuario = Usuario.builder().id(20).keycloakId("sub-prof").role(RoleEnum.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(99).nome("Outro").build();
        Agendamento agendamento = agendamentoPai(100);
        Agenda ag = agenda();
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).estabelecimentoId(40).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(20)).thenReturn(Optional.of(profissional));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));

        assertThatThrownBy(() -> useCase.executar(1, "sub-prof"))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void cancelarComoProfissional_statusNaoAgendado_lancaOperacaoInvalida() {
        Usuario usuario = Usuario.builder().id(20).keycloakId("sub-prof").role(RoleEnum.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(30).build();
        Agendamento agendamento = Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(null)
                .clienteId(100).status(StatusAgendamentoEnum.DISPONIVEL).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-prof")).thenReturn(Optional.of(usuario));
        when(profissionalPort.buscarPorUsuarioId(20)).thenReturn(Optional.of(profissional));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> useCase.executar(1, "sub-prof"))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void cancelarComoAdmin_semCliente_semEmail() {
        Usuario admin = Usuario.builder().id(99).keycloakId("sub-admin").role(RoleEnum.ADMIN).build();
        Agendamento agendamento = Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(null)
                .clienteId(null).servicoId(null)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0))
                .status(StatusAgendamentoEnum.AGENDADO).build();
        Agenda ag = agenda();

        when(usuarioPort.buscarPorCodKeycloak("sub-admin")).thenReturn(Optional.of(admin));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of());
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Agendamento result = useCase.executar(1, "sub-admin");

        assertThat(result).isNotNull();
    }

    @Test
    void cancelarComoAdmin_comFilhos_cancela() {
        Usuario admin = Usuario.builder().id(99).keycloakId("sub-admin").role(RoleEnum.ADMIN).build();
        Agendamento pai = Agendamento.builder().id(1).agendaId(7).agendamentoPaiId(null)
                .clienteId(100).servicoId(5)
                .horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(10, 0))
                .status(StatusAgendamentoEnum.AGENDADO).build();
        Agendamento filho = Agendamento.builder().id(2).agendaId(7).agendamentoPaiId(1)
                .status(StatusAgendamentoEnum.AGENDADO).build();
        Agenda ag = agenda();
        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(20).profissionalId(30).build();

        when(usuarioPort.buscarPorCodKeycloak("sub-admin")).thenReturn(Optional.of(admin));
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(pai));
        when(agendaPort.buscarPorId(7)).thenReturn(Optional.of(ag));
        when(agendamentoPort.buscarFilhosPorPaiId(1)).thenReturn(List.of(filho));
        when(agendamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clientePort.buscarPorId(100)).thenReturn(Optional.of(
                Cliente.builder().id(100).email("c@x.com").build()));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.empty());
        when(profissionalVinculoPort.buscarPorId(20)).thenReturn(Optional.of(vinculo));
        when(profissionalPort.buscarPorId(30)).thenReturn(Optional.empty());

        Agendamento result = useCase.executar(1, "sub-admin");

        assertThat(result).isNotNull();
    }

    @Test
    void executar_roleDesconhecida_lancaAcessoNegado() {
        // Simula role não mapeada (ex.: algum futuro enum não tratado)
        Usuario usuario = Usuario.builder().id(1).keycloakId("sub").role(null).build();
        when(usuarioPort.buscarPorCodKeycloak("sub")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> useCase.executar(1, "sub"))
                .isInstanceOf(AcessoNegadoException.class);
    }
}
