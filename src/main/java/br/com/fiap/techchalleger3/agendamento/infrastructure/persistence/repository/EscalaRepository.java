package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EscalaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EscalaRepository extends JpaRepository<EscalaEntity, Integer> {
    List<EscalaEntity> findByCodProfissionalVinculo(Integer codProfissionalVinculo);

    @Query("SELECT e FROM EscalaEntity e WHERE " +
           "(:estabelecimentoId IS NULL OR e.codEstabelecimento = :estabelecimentoId) AND " +
           "(:profissionalVinculoId IS NULL OR e.codProfissionalVinculo = :profissionalVinculoId)")
    List<EscalaEntity> listarPorFiltros(
            @Param("estabelecimentoId") Integer estabelecimentoId,
            @Param("profissionalVinculoId") Integer profissionalVinculoId);
}
