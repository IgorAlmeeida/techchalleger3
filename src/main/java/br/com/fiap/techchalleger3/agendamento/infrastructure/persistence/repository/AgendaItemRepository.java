package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgendaItemRepository extends JpaRepository<AgendaItemEntity, Integer> {
    List<AgendaItemEntity> findByCodAgenda(Integer codAgenda);
    boolean existsByCodAgendaInAndCodServico(List<Integer> codAgendas, Integer codServico);
}
