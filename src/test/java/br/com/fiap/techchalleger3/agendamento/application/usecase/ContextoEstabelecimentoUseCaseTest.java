package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalDisponivelResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextoEstabelecimentoUseCaseTest {

    // ListarEstabelecimentosContextoUseCase
    @Mock private UsuarioRepositoryPort usuarioPort;
    @Mock private ProfissionalRepositoryPort profissionalPort;
    @Mock private ProfissionalVinculoRepositoryPort vinculoPort;
    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @InjectMocks private ListarEstabelecimentosContextoUseCase listarContexto;

    // ListarOfertaDoEstabelecimentoUseCase
    @Mock private ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    @Mock private ServicoRepositoryPort servicoPort;
    @InjectMocks private ListarOfertaDoEstabelecimentoUseCase listarOferta;

    @Test
    void contexto_lancaQuandoUsuarioNaoExiste() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listarContexto.executar("kc"))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void contexto_admin_retornaTodosAtivos() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(1).role(RoleEnum.ADMIN).build()));
        when(estabelecimentoPort.listarAtivos()).thenReturn(List.of(Estabelecimento.builder().id(10).build()));

        List<Estabelecimento> result = listarContexto.executar("kc");

        assertThat(result).hasSize(1);
    }

    @Test
    void contexto_profissional_retornaVinculados() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(1).role(RoleEnum.PROFISSIONAL).build()));
        when(profissionalPort.buscarPorUsuarioId(1)).thenReturn(Optional.of(Profissional.builder().id(10).build()));
        when(vinculoPort.listarEstabelecimentoIdsAtivosPorProfissional(10)).thenReturn(List.of(20));
        when(estabelecimentoPort.listarPorIds(List.of(20))).thenReturn(List.of(Estabelecimento.builder().id(20).nome("Studio").build()));

        List<Estabelecimento> result = listarContexto.executar("kc");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(20);
    }

    @Test
    void contexto_profissional_semVinculos_retornaVazio() {
        when(usuarioPort.buscarPorCodKeycloak("kc")).thenReturn(Optional.of(Usuario.builder().id(1).role(RoleEnum.PROFISSIONAL).build()));
        when(profissionalPort.buscarPorUsuarioId(1)).thenReturn(Optional.of(Profissional.builder().id(10).build()));
        when(vinculoPort.listarEstabelecimentoIdsAtivosPorProfissional(10)).thenReturn(List.of());

        List<Estabelecimento> result = listarContexto.executar("kc");

        assertThat(result).isEmpty();
    }

    @Test
    void oferta_lancaQuandoEstabelecimentoNaoExiste() {
        when(estabelecimentoPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listarOferta.executar(99, null))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void oferta_retornaProfissionaisComServicos() {
        Estabelecimento estab = Estabelecimento.builder().id(1).nome("Studio").build();
        when(estabelecimentoPort.buscarPorId(1)).thenReturn(Optional.of(estab));
        when(vinculoPort.listarIdsPorEstabelecimento(1)).thenReturn(List.of(10));

        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).estabelecimentoId(1).build();
        when(vinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));

        ProfissionalVinculoServico pvs = ProfissionalVinculoServico.builder().id(1).servicoId(5).profissionalVinculoId(10).build();
        when(vinculoServicoPort.listarPorProfissionalVinculoId(10)).thenReturn(List.of(pvs));
        when(servicoPort.buscarPorId(5)).thenReturn(Optional.of(Servico.builder().id(5).nome("Corte").duracaoMinutos(30).preco(BigDecimal.TEN).build()));
        when(profissionalPort.buscarPorId(20)).thenReturn(Optional.of(Profissional.builder().id(20).nome("Dr.").build()));

        List<ProfissionalDisponivelResponse> result = listarOferta.executar(1, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).servicos()).hasSize(1);
    }

    @Test
    void oferta_filtraPorServico_retornaVazioQuandoNaoCorresponde() {
        Estabelecimento estab = Estabelecimento.builder().id(1).nome("Studio").build();
        when(estabelecimentoPort.buscarPorId(1)).thenReturn(Optional.of(estab));
        when(vinculoPort.listarIdsPorEstabelecimento(1)).thenReturn(List.of(10));

        ProfissionalVinculo vinculo = ProfissionalVinculo.builder().id(10).profissionalId(20).estabelecimentoId(1).build();
        when(vinculoPort.buscarPorId(10)).thenReturn(Optional.of(vinculo));

        ProfissionalVinculoServico pvs = ProfissionalVinculoServico.builder().id(1).servicoId(5).profissionalVinculoId(10).build();
        when(vinculoServicoPort.listarPorProfissionalVinculoId(10)).thenReturn(List.of(pvs));

        // Filter for servicoId=99 which doesn't match servicoId=5
        List<ProfissionalDisponivelResponse> result = listarOferta.executar(1, 99);

        assertThat(result).isEmpty();
    }
}
