package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AgendaRepository extends JpaRepository<AgendaEntity, Integer> {
    List<AgendaEntity> findByCodProfissionalVinculo(Integer codProfissionalVinculo);
    List<AgendaEntity> findByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(Integer codProfissionalVinculo, LocalDate data);
    boolean existsByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(Integer codProfissionalVinculo, LocalDate data);
    boolean existsByCodProfissionalVinculoAndDataAgenda(Integer codProfissionalVinculo, LocalDate dataAgenda);

    @Query("SELECT a FROM AgendaEntity a WHERE " +
           "(:profissionalVinculoId IS NULL OR a.codProfissionalVinculo = :profissionalVinculoId) AND " +
           "(:estabelecimentoId IS NULL OR a.codEstabelecimento = :estabelecimentoId) AND " +
           "(:dataInicio IS NULL OR a.dataAgenda >= :dataInicio) AND " +
           "(:dataFim IS NULL OR a.dataAgenda <= :dataFim)")
    Page<AgendaEntity> listarPorFiltros(
            @Param("profissionalVinculoId") Integer profissionalVinculoId,
            @Param("estabelecimentoId") Integer estabelecimentoId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            Pageable pageable);
}
