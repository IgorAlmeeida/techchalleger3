package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.domain.model.IntegracaoCalendarioExterno;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProvedorCalendarioEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.IntegracaoCalendarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegracaoCalendarioRepositoryPortImplTest {

    @Mock private IntegracaoCalendarioRepository repository;
    @InjectMocks private IntegracaoCalendarioRepositoryPortImpl portImpl;

    private IntegracaoCalendarioExterno dominio() {
        return IntegracaoCalendarioExterno.builder()
                .id(1)
                .clienteId(10)
                .profissionalId(null)
                .provedor(ProvedorCalendarioEnum.GOOGLE)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiraEm(Instant.now().plusSeconds(3600))
                .ativo(true)
                .build();
    }

    private IntegracaoCalendarioEntity entity() {
        return IntegracaoCalendarioEntity.builder()
                .codigo(1)
                .codCliente(10)
                .codProfissional(null)
                .provedor(ProvedorCalendarioEnum.GOOGLE)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiraEm(Instant.now().plusSeconds(3600))
                .ativo(true)
                .build();
    }

    @Test
    void salvar_convertePraEntidadeEDevolveDominio() {
        when(repository.save(any(IntegracaoCalendarioEntity.class))).thenReturn(entity());

        IntegracaoCalendarioExterno resultado = portImpl.salvar(dominio());

        assertThat(resultado.getId()).isEqualTo(1);
        assertThat(resultado.getClienteId()).isEqualTo(10);
        assertThat(resultado.getAccessToken()).isEqualTo("access-token");
        assertThat(resultado.getProvedor()).isEqualTo(ProvedorCalendarioEnum.GOOGLE);
    }

    @Test
    void buscarAtivaByClienteId_encontrada_retornaDominioMapeado() {
        when(repository.findByCodClienteAndAtivoTrue(10)).thenReturn(Optional.of(entity()));

        Optional<IntegracaoCalendarioExterno> resultado = portImpl.buscarAtivaByClienteId(10);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getClienteId()).isEqualTo(10);
    }

    @Test
    void buscarAtivaByClienteId_naoEncontrada_retornaVazio() {
        when(repository.findByCodClienteAndAtivoTrue(99)).thenReturn(Optional.empty());

        Optional<IntegracaoCalendarioExterno> resultado = portImpl.buscarAtivaByClienteId(99);

        assertThat(resultado).isEmpty();
    }

    @Test
    void buscarAtivaByProfissionalId_encontrada_retornaDominioMapeado() {
        IntegracaoCalendarioEntity e = IntegracaoCalendarioEntity.builder()
                .codigo(2).codProfissional(20).provedor(ProvedorCalendarioEnum.GOOGLE)
                .accessToken("a").refreshToken("r").ativo(true).build();
        when(repository.findByCodProfissionalAndAtivoTrue(20)).thenReturn(Optional.of(e));

        Optional<IntegracaoCalendarioExterno> resultado = portImpl.buscarAtivaByProfissionalId(20);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getProfissionalId()).isEqualTo(20);
    }

    @Test
    void buscarAtivaByProfissionalId_naoEncontrada_retornaVazio() {
        when(repository.findByCodProfissionalAndAtivoTrue(99)).thenReturn(Optional.empty());

        Optional<IntegracaoCalendarioExterno> resultado = portImpl.buscarAtivaByProfissionalId(99);

        assertThat(resultado).isEmpty();
    }

    @Test
    void desativarPorClienteId_delegaParaRepository() {
        portImpl.desativarPorClienteId(10);

        verify(repository).desativarPorCodCliente(10);
    }

    @Test
    void desativarPorProfissionalId_delegaParaRepository() {
        portImpl.desativarPorProfissionalId(20);

        verify(repository).desativarPorCodProfissional(20);
    }
}
