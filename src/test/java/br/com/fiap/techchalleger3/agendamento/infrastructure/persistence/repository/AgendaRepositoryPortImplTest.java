package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.AgendaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cobre os métodos de delegação simples do adapter. A Specification usada em
 * {@code listarPorFiltros} (predicados montados via CriteriaBuilder) só executa de
 * fato contra um EntityManager real — precisa de teste de integração (Testcontainers)
 * para cobrir os 4 branches condicionais dela; fora do escopo deste teste unitário.
 */
@ExtendWith(MockitoExtension.class)
class AgendaRepositoryPortImplTest {

    @Mock private AgendaRepository repository;
    @Mock private AgendaMapper mapper;
    @InjectMocks private AgendaRepositoryPortImpl portImpl;

    private AgendaEntity entity() {
        return AgendaEntity.builder().codigo(1).codProfissionalVinculo(10)
                .dataAgenda(LocalDate.now().plusDays(1)).build();
    }

    private Agenda dominio() {
        return Agenda.builder().id(1).profissionalVinculoId(10)
                .dataAgenda(LocalDate.now().plusDays(1)).build();
    }

    @Test
    void buscarPorId_encontrada_retornaMapeada() {
        when(repository.findById(1)).thenReturn(Optional.of(entity()));
        when(mapper.toModel(any(AgendaEntity.class))).thenReturn(dominio());

        Optional<Agenda> resultado = portImpl.buscarPorId(1);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1);
    }

    @Test
    void buscarPorId_naoEncontrada_retornaVazio() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThat(portImpl.buscarPorId(99)).isEmpty();
    }

    @Test
    void listarPorProfissionalVinculoId_retornaListaMapeada() {
        when(repository.findByCodProfissionalVinculo(10)).thenReturn(List.of(entity()));
        when(mapper.toModel(any(AgendaEntity.class))).thenReturn(dominio());

        List<Agenda> resultado = portImpl.listarPorProfissionalVinculoId(10);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void listarFuturasPorVinculo_delegaParaRepositoryComDataAtual() {
        when(repository.findByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(eq(10), any(LocalDate.class)))
                .thenReturn(List.of(entity()));
        when(mapper.toModel(any(AgendaEntity.class))).thenReturn(dominio());

        List<Agenda> resultado = portImpl.listarFuturasPorVinculo(10);

        assertThat(resultado).hasSize(1);
        verify(repository).findByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(eq(10), any(LocalDate.class));
    }

    @Test
    void existeAgendaFuturaPorVinculo_true() {
        when(repository.existsByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(eq(10), any(LocalDate.class)))
                .thenReturn(true);

        assertThat(portImpl.existeAgendaFuturaPorVinculo(10)).isTrue();
    }

    @Test
    void existeAgendaFuturaPorVinculo_false() {
        when(repository.existsByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(eq(10), any(LocalDate.class)))
                .thenReturn(false);

        assertThat(portImpl.existeAgendaFuturaPorVinculo(10)).isFalse();
    }

    @Test
    void existeAgendaPorVinculoEData_true() {
        LocalDate data = LocalDate.now().plusDays(2);
        when(repository.existsByCodProfissionalVinculoAndDataAgenda(10, data)).thenReturn(true);

        assertThat(portImpl.existeAgendaPorVinculoEData(10, data)).isTrue();
    }

    @Test
    void salvar_convertePraEntidadeEDevolveDominio() {
        when(mapper.toEntity(any(Agenda.class))).thenReturn(entity());
        when(repository.save(any(AgendaEntity.class))).thenReturn(entity());
        when(mapper.toModel(any(AgendaEntity.class))).thenReturn(dominio());

        Agenda resultado = portImpl.salvar(dominio());

        assertThat(resultado.getId()).isEqualTo(1);
    }

    @Test
    void deletar_delegaParaRepository() {
        portImpl.deletar(1);

        verify(repository).deleteById(1);
    }
}
