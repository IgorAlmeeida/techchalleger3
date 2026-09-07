package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AgendaRepositoryPort {
    Optional<Agenda> buscarPorId(Integer id);
    List<Agenda> listarPorProfissionalVinculoId(Integer profissionalVinculoId);
    Page<Agenda> listarPorFiltros(Integer profissionalVinculoId, Integer estabelecimentoId, LocalDate dataInicio, LocalDate dataFim, Pageable pageable);
    List<Agenda> listarFuturasPorVinculo(Integer profissionalVinculoId);
    boolean existeAgendaFuturaPorVinculo(Integer profissionalVinculoId);
    boolean existeAgendaPorVinculoEData(Integer profissionalVinculoId, LocalDate dataAgenda);
    Agenda salvar(Agenda agenda);
}
