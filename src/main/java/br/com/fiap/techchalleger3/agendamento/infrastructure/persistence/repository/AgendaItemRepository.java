package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AgendaItemRepository extends JpaRepository<AgendaItemEntity, Integer> {
    List<AgendaItemEntity> findByCodAgenda(Integer codAgenda);
    boolean existsByCodAgendaInAndCodServico(List<Integer> codAgendas, Integer codServico);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AgendaItemEntity a WHERE a.codAgenda = :agendaId")
    void deletarPorAgendaId(@Param("agendaId") Integer agendaId);
}
