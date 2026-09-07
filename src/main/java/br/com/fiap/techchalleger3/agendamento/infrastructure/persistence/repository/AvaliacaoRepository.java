package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AvaliacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<AvaliacaoEntity, Integer> {
    List<AvaliacaoEntity> findByCodEstabelecimento(Integer codEstabelecimento);
    List<AvaliacaoEntity> findByCodProfissionalVinculo(Integer codProfissionalVinculo);
    boolean existsByCodAgendamento(Integer codAgendamento);

    @Query("SELECT COALESCE(AVG(a.nota), 0.0) FROM AvaliacaoEntity a WHERE a.codEstabelecimento = :id")
    double calcularNotaMedia(@Param("id") Integer estabelecimentoId);
}
