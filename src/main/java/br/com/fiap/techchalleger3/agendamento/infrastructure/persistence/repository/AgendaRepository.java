package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface AgendaRepository extends JpaRepository<AgendaEntity, Integer>, JpaSpecificationExecutor<AgendaEntity> {
    List<AgendaEntity> findByCodProfissionalVinculo(Integer codProfissionalVinculo);
    List<AgendaEntity> findByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(Integer codProfissionalVinculo, LocalDate data);
    boolean existsByCodProfissionalVinculoAndDataAgendaGreaterThanEqual(Integer codProfissionalVinculo, LocalDate data);
    boolean existsByCodProfissionalVinculoAndDataAgenda(Integer codProfissionalVinculo, LocalDate dataAgenda);
}
