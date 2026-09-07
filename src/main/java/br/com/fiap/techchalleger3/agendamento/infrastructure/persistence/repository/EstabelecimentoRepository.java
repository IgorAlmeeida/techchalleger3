package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EstabelecimentoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EstabelecimentoRepository extends JpaRepository<EstabelecimentoEntity, Integer> {
    boolean existsByCnpj(String cnpj);
    boolean existsByCnpjAndCodigoNot(String cnpj, Integer codigo);
    Page<EstabelecimentoEntity> findAllByAtivo(Boolean ativo, Pageable pageable);
    List<EstabelecimentoEntity> findAllByAtivo(Boolean ativo);
    List<EstabelecimentoEntity> findAllByCodigoIn(List<Integer> ids);

    @Query("SELECT e FROM EstabelecimentoEntity e WHERE e.ativo = true "
            + "AND (:nome IS NULL OR LOWER(e.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) "
            + "AND (:localizacao IS NULL OR LOWER(e.endereco) LIKE LOWER(CONCAT('%', :localizacao, '%')))")
    Page<EstabelecimentoEntity> buscarComFiltros(
            @Param("nome") String nome,
            @Param("localizacao") String localizacao,
            Pageable pageable);
}
