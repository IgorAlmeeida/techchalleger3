package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.AgendaMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaRepositoryPortImplMoreTest {

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
    void existeAgendaPorVinculoEData_false() {
        LocalDate data = LocalDate.now().plusDays(3);
        when(repository.existsByCodProfissionalVinculoAndDataAgenda(10, data)).thenReturn(false);

        assertThat(portImpl.existeAgendaPorVinculoEData(10, data)).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    void listarPorFiltros_semFiltros_delegaParaFindAll() {
        Page<AgendaEntity> pageEntity = new PageImpl<>(List.of(entity()));
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageEntity);
        when(mapper.toModel(any(AgendaEntity.class))).thenReturn(dominio());

        Page<Agenda> result = portImpl.listarPorFiltros(null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void listarPorFiltros_comTodosOsFiltros_invocaSpecificationCorretamente() {
        Page<AgendaEntity> pageEntity = new PageImpl<>(List.of(entity()));
        ArgumentCaptor<Specification<AgendaEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        when(repository.findAll(captor.capture(), any(Pageable.class))).thenReturn(pageEntity);
        when(mapper.toModel(any(AgendaEntity.class))).thenReturn(dominio());

        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusDays(30);
        portImpl.listarPorFiltros(10, 5, inicio, fim, PageRequest.of(0, 10));

        // Invoca o predicate da Specification com mocks do JPA Criteria API
        Specification<AgendaEntity> spec = captor.getValue();
        Root<AgendaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> pathVinculo = mock(Path.class);
        Path<Object> pathEstab = mock(Path.class);
        Path<Object> pathInicio = mock(Path.class);
        Path<Object> pathFim = mock(Path.class);
        Predicate predVinculo = mock(Predicate.class);
        Predicate predEstab = mock(Predicate.class);
        Predicate predInicio = mock(Predicate.class);
        Predicate predFim = mock(Predicate.class);
        Predicate predAnd = mock(Predicate.class);

        when(root.get("codProfissionalVinculo")).thenReturn(pathVinculo);
        when(root.get("codEstabelecimento")).thenReturn(pathEstab);
        when(root.get("dataAgenda")).thenReturn(pathInicio);
        when(cb.equal(pathVinculo, 10)).thenReturn(predVinculo);
        when(cb.equal(pathEstab, 5)).thenReturn(predEstab);
        when(cb.greaterThanOrEqualTo(any(), (Comparable) any())).thenReturn(predInicio);
        when(cb.lessThanOrEqualTo(any(), (Comparable) any())).thenReturn(predFim);
        when(cb.and(any(Predicate[].class))).thenReturn(predAnd);

        Predicate result = spec.toPredicate(root, query, cb);
        assertThat(result).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void listarPorFiltros_comSomenteVinculo_invocaSpecificationCorretamente() {
        Page<AgendaEntity> pageEntity = new PageImpl<>(List.of());
        ArgumentCaptor<Specification<AgendaEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        when(repository.findAll(captor.capture(), any(Pageable.class))).thenReturn(pageEntity);

        portImpl.listarPorFiltros(10, null, null, null, PageRequest.of(0, 10));

        Specification<AgendaEntity> spec = captor.getValue();
        Root<AgendaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> pathVinculo = mock(Path.class);
        Predicate predVinculo = mock(Predicate.class);
        Predicate predAnd = mock(Predicate.class);

        when(root.get("codProfissionalVinculo")).thenReturn(pathVinculo);
        when(cb.equal(pathVinculo, 10)).thenReturn(predVinculo);
        when(cb.and(any(Predicate[].class))).thenReturn(predAnd);

        Predicate result = spec.toPredicate(root, query, cb);
        assertThat(result).isNotNull();
    }
}
