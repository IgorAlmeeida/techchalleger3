package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepositoryPort {
    Optional<Agendamento> buscarPorId(Integer id);
    List<Agendamento> listarPorAgendaId(Integer agendaId);
    List<Agendamento> buscarDisponiveisPorVinculo(Integer profissionalVinculoId);
    List<Agendamento> buscarFilhosPorPaiId(Integer paiId);
    List<Agendamento> buscarAgendadosPassados();
    List<Agendamento> buscarAgendadosPorClienteNaData(Integer clienteId, LocalDate dataAgenda);
    boolean existeAgendadoPorClienteVinculoServico(Integer clienteId, Integer profissionalVinculoId, Integer servicoId);
    List<Agendamento> buscarPaisPorClienteId(Integer clienteId, List<StatusAgendamentoEnum> statuses, LocalDate dataInicio, LocalDate dataFim);
    List<Agendamento> buscarPaisPorProfissionalVinculoIds(List<Integer> profissionalVinculoIds, List<StatusAgendamentoEnum> statuses, LocalDate dataInicio, LocalDate dataFim);
    Agendamento salvar(Agendamento agendamento);
}
