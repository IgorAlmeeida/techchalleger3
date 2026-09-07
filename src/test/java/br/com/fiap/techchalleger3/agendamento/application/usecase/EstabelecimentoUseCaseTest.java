package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.CachePort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstabelecimentoUseCaseTest {

    @Mock private EstabelecimentoRepositoryPort estabelecimentoPort;
    @Mock private ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    @Mock private CachePort cachePort;
    @InjectMocks private EstabelecimentoUseCase useCase;

    private Estabelecimento estab(int id) {
        return Estabelecimento.builder().id(id).nome("Studio " + id).cnpj("00.000.000/0001-0" + id).ativo(true).build();
    }

    @Test
    void deveCriar_quandoCnpjNaoExiste() {
        when(estabelecimentoPort.existePorCnpj("00.000.000/0001-00")).thenReturn(false);
        Estabelecimento salvo = estab(1);
        when(estabelecimentoPort.salvar(any())).thenReturn(salvo);

        Estabelecimento result = useCase.criar("Studio 1", "00.000.000/0001-00", null, null, null, null, null);

        assertThat(result.getId()).isEqualTo(1);
        verify(cachePort).invalidar(any());
    }

    @Test
    void deveLancar_quandoCnpjJaExiste() {
        when(estabelecimentoPort.existePorCnpj("dup")).thenReturn(true);

        assertThatThrownBy(() -> useCase.criar("X", "dup", null, null, null, null, null))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void deveListar_buscandoDoCache() {
        Estabelecimento e = estab(1);
        when(cachePort.get(any(), any())).thenReturn(Optional.of(List.of(e)));

        Page<Estabelecimento> page = useCase.listar(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void deveListar_buscandoDoBanco_quandoCacheVazio() {
        when(cachePort.get(any(), any())).thenReturn(Optional.empty());
        when(estabelecimentoPort.listarAtivos()).thenReturn(List.of(estab(1), estab(2)));

        Page<Estabelecimento> page = useCase.listar(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        verify(cachePort).put(any(), any(), any());
    }

    @Test
    void deveBuscarPorId_quandoExiste() {
        when(estabelecimentoPort.buscarPorId(1)).thenReturn(Optional.of(estab(1)));

        Estabelecimento result = useCase.buscarPorId(1);

        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void deveLancar_quandoIdNaoExiste() {
        when(estabelecimentoPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.buscarPorId(99))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveAtualizar_quandoValido() {
        Estabelecimento existente = estab(1);
        when(estabelecimentoPort.buscarPorId(1)).thenReturn(Optional.of(existente));
        when(estabelecimentoPort.existePorCnpjExcluindoId("novo-cnpj", 1)).thenReturn(false);
        when(estabelecimentoPort.salvar(any())).thenReturn(existente);

        Estabelecimento result = useCase.atualizar(1, "Novo Nome", "novo-cnpj", null, null, null, null, null);

        assertThat(result).isNotNull();
        verify(cachePort).invalidar(any());
    }

    @Test
    void deveInativar_quandoSemVinculos() {
        Estabelecimento existente = estab(1);
        when(estabelecimentoPort.buscarPorId(1)).thenReturn(Optional.of(existente));
        when(profissionalVinculoPort.existeVinculoAtivoPorEstabelecimento(1)).thenReturn(false);

        useCase.inativar(1);

        verify(estabelecimentoPort).salvar(any());
        verify(cachePort).invalidar(any());
    }

    @Test
    void deveLancar_quandoAtualizarComCnpjDuplicado() {
        when(estabelecimentoPort.buscarPorId(1)).thenReturn(Optional.of(estab(1)));
        when(estabelecimentoPort.existePorCnpjExcluindoId("dup-cnpj", 1)).thenReturn(true);

        assertThatThrownBy(() -> useCase.atualizar(1, "Nome", "dup-cnpj", null, null, null, null, null))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void deveLancar_quandoInativarComVinculos() {
        when(estabelecimentoPort.buscarPorId(1)).thenReturn(Optional.of(estab(1)));
        when(profissionalVinculoPort.existeVinculoAtivoPorEstabelecimento(1)).thenReturn(true);

        assertThatThrownBy(() -> useCase.inativar(1))
                .isInstanceOf(OperacaoInvalidaException.class);
    }
}
