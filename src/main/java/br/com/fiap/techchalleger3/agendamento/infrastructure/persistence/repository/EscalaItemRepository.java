package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EscalaItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscalaItemRepository extends JpaRepository<EscalaItemEntity, Integer> {
    List<EscalaItemEntity> findByCodEscala(Integer codEscala);
    List<EscalaItemEntity> findByCodEscalaAndAtivaTrue(Integer codEscala);
    void deleteByCodEscala(Integer codEscala);
}
