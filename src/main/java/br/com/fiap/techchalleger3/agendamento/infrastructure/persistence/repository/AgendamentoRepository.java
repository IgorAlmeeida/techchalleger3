package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<AgendamentoEntity, Integer> {
    List<AgendamentoEntity> findByCodAgenda(Integer codAgenda);
    List<AgendamentoEntity> findByCodAgendaAndStatus(Integer codAgenda, StatusAgendamentoEnum status);
    List<AgendamentoEntity> findByCodCliente(Integer codCliente);
    List<AgendamentoEntity> findByCodAgendaInAndStatus(List<Integer> codAgendas, StatusAgendamentoEnum status);
    List<AgendamentoEntity> findByStatus(StatusAgendamentoEnum status);
    List<AgendamentoEntity> findByCodAgendamentoPai(Integer codAgendamentoPai);

    @Query("SELECT a FROM AgendamentoEntity a JOIN AgendaEntity ag ON a.codAgenda = ag.codigo " +
           "WHERE a.codCliente = :clienteId AND a.status = :status AND a.codAgendamentoPai IS NULL " +
           "AND ag.dataAgenda = :dataAgenda")
    List<AgendamentoEntity> findAgendadosPaisDoPacienteNaData(
            @Param("clienteId") Integer clienteId,
            @Param("status") StatusAgendamentoEnum status,
            @Param("dataAgenda") LocalDate dataAgenda);

    @Query("SELECT COUNT(a) > 0 FROM AgendamentoEntity a JOIN AgendaEntity ag ON a.codAgenda = ag.codigo " +
           "WHERE a.codCliente = :clienteId AND a.codServico = :servicoId " +
           "AND ag.codProfissionalVinculo = :profissionalVinculoId AND a.status = :status " +
           "AND a.codAgendamentoPai IS NULL")
    boolean existeAgendadoPorClienteVinculoServico(
            @Param("clienteId") Integer clienteId,
            @Param("profissionalVinculoId") Integer profissionalVinculoId,
            @Param("servicoId") Integer servicoId,
            @Param("status") StatusAgendamentoEnum status);

    @Query("SELECT a FROM AgendamentoEntity a JOIN AgendaEntity ag ON a.codAgenda = ag.codigo " +
           "WHERE a.codCliente = :clienteId AND a.codAgendamentoPai IS NULL AND a.status IN :statuses " +
           "AND (:dataInicio IS NULL OR ag.dataAgenda >= :dataInicio) " +
           "AND (:dataFim IS NULL OR ag.dataAgenda <= :dataFim) " +
           "ORDER BY ag.dataAgenda ASC, a.horaInicio ASC")
    List<AgendamentoEntity> buscarPaisDoPaciente(
            @Param("clienteId") Integer clienteId,
            @Param("statuses") List<StatusAgendamentoEnum> statuses,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    @Query("SELECT a FROM AgendamentoEntity a JOIN AgendaEntity ag ON a.codAgenda = ag.codigo " +
           "WHERE ag.codProfissionalVinculo IN :vinculoIds AND a.codAgendamentoPai IS NULL AND a.status IN :statuses " +
           "AND (:dataInicio IS NULL OR ag.dataAgenda >= :dataInicio) " +
           "AND (:dataFim IS NULL OR ag.dataAgenda <= :dataFim) " +
           "ORDER BY ag.dataAgenda ASC, a.horaInicio ASC")
    List<AgendamentoEntity> buscarPaisDosProfissionaisVinculos(
            @Param("vinculoIds") List<Integer> vinculoIds,
            @Param("statuses") List<StatusAgendamentoEnum> statuses,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);
}
