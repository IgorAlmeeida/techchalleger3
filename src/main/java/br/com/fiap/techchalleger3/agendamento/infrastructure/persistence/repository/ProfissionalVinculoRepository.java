package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalVinculoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfissionalVinculoRepository extends JpaRepository<ProfissionalVinculoEntity, Integer> {
    List<ProfissionalVinculoEntity> findByCodProfissional(Integer codProfissional);
    List<ProfissionalVinculoEntity> findByCodProfissionalAndDataFimIsNull(Integer codProfissional);
    List<ProfissionalVinculoEntity> findByCodEstabelecimento(Integer codEstabelecimento);
    boolean existsByCodProfissionalAndCodEstabelecimentoAndDataFimIsNull(Integer codProfissional, Integer codEstabelecimento);
    boolean existsByCodEstabelecimentoAndDataFimIsNull(Integer codEstabelecimento);

    @Query("SELECT v FROM ProfissionalVinculoEntity v WHERE " +
           "(:profissionalId IS NULL OR v.codProfissional = :profissionalId) AND " +
           "(:estabelecimentoId IS NULL OR v.codEstabelecimento = :estabelecimentoId)")
    Page<ProfissionalVinculoEntity> listarPorFiltros(
            @Param("profissionalId") Integer profissionalId,
            @Param("estabelecimentoId") Integer estabelecimentoId,
            Pageable pageable);
}
