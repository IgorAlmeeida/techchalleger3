package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalVinculoServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProfissionalVinculoServicoRepository extends JpaRepository<ProfissionalVinculoServicoEntity, Integer> {
    List<ProfissionalVinculoServicoEntity> findByCodProfissionalVinculo(Integer codProfissionalVinculo);
    boolean existsByCodProfissionalVinculoAndCodServico(Integer codProfissionalVinculo, Integer codServico);
    Optional<ProfissionalVinculoServicoEntity> findByCodProfissionalVinculoAndCodServico(Integer codProfissionalVinculo, Integer codServico);

    @Transactional
    void deleteByCodProfissionalVinculoAndCodServico(Integer codProfissionalVinculo, Integer codServico);
}
