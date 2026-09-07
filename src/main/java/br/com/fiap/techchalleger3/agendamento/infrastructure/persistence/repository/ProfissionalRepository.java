package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfissionalRepository extends JpaRepository<ProfissionalEntity, Integer> {
    Optional<ProfissionalEntity> findByCodUsuario(Integer codUsuario);
    Optional<ProfissionalEntity> findByEmail(String email);

    @Query("SELECT p FROM ProfissionalEntity p WHERE " +
           "(:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:especialidades IS NULL OR LOWER(p.especialidades) LIKE LOWER(CONCAT('%', :especialidades, '%'))) AND " +
           "(:incluirInativos = true OR p.ativo = true)")
    Page<ProfissionalEntity> buscarComFiltros(@Param("nome") String nome,
                                              @Param("especialidades") String especialidades,
                                              @Param("incluirInativos") boolean incluirInativos,
                                              Pageable pageable);
}
