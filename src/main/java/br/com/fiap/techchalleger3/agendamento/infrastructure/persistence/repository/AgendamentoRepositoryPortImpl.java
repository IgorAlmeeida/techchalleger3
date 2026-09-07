package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.AgendamentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AgendamentoRepositoryPortImpl implements AgendamentoRepositoryPort {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendaRepository agendaRepository;
    private final AgendamentoMapper mapper;

    @Override
    public Optional<Agendamento> buscarPorId(Integer id) {
        return agendamentoRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Agendamento> listarPorAgendaId(Integer agendaId) {
        return agendamentoRepository.findByCodAgenda(agendaId).stream().map(mapper::toModel).toList();
    }

    @Override
    public List<Agendamento> buscarDisponiveisPorVinculo(Integer profissionalVinculoId) {
        List<AgendaEntity> agendas = agendaRepository.findByCodProfissionalVinculo(profissionalVinculoId);
        if (agendas.isEmpty()) {
            return List.of();
        }
        List<Integer> agendaIds = agendas.stream().map(AgendaEntity::getCodigo).toList();
        return agendamentoRepository.findByCodAgendaInAndStatus(agendaIds, StatusAgendamentoEnum.DISPONIVEL)
                .stream()
                .map(mapper::toModel)
                .sorted(Comparator.comparing(a -> getDataAgenda(agendas, a.getAgendaId())))
                .toList();
    }

    @Override
    public List<Agendamento> buscarAgendadosPassados() {
        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        return agendamentoRepository.findByStatus(StatusAgendamentoEnum.AGENDADO)
                .stream()
                .filter(e -> {
                    AgendaEntity agenda = agendaRepository.findById(e.getCodAgenda()).orElse(null);
                    if (agenda == null) return false;
                    LocalDate dataAgenda = agenda.getDataAgenda();
                    return dataAgenda.isBefore(hoje) ||
                            (dataAgenda.equals(hoje) && e.getHoraFim().isBefore(agora));
                })
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public List<Agendamento> buscarFilhosPorPaiId(Integer paiId) {
        return agendamentoRepository.findByCodAgendamentoPai(paiId)
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public List<Agendamento> buscarAgendadosPorClienteNaData(Integer clienteId, LocalDate dataAgenda) {
        return agendamentoRepository.findAgendadosPaisDoPacienteNaData(clienteId, StatusAgendamentoEnum.AGENDADO, dataAgenda)
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public boolean existeAgendadoPorClienteVinculoServico(Integer clienteId, Integer profissionalVinculoId, Integer servicoId) {
        return agendamentoRepository.existeAgendadoPorClienteVinculoServico(
                clienteId, profissionalVinculoId, servicoId, StatusAgendamentoEnum.AGENDADO);
    }

    @Override
    public List<Agendamento> buscarPaisPorClienteId(Integer clienteId, List<StatusAgendamentoEnum> statuses, LocalDate dataInicio, LocalDate dataFim) {
        return agendamentoRepository.buscarPaisDoPaciente(clienteId, statuses, dataInicio, dataFim)
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public List<Agendamento> buscarPaisPorProfissionalVinculoIds(List<Integer> profissionalVinculoIds, List<StatusAgendamentoEnum> statuses, LocalDate dataInicio, LocalDate dataFim) {
        if (profissionalVinculoIds == null || profissionalVinculoIds.isEmpty()) return List.of();
        return agendamentoRepository.buscarPaisDosProfissionaisVinculos(profissionalVinculoIds, statuses, dataInicio, dataFim)
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        return mapper.toModel(agendamentoRepository.save(mapper.toEntity(agendamento)));
    }

    private LocalDate getDataAgenda(List<AgendaEntity> agendas, Integer agendaId) {
        return agendas.stream()
                .filter(e -> e.getCodigo().equals(agendaId))
                .findFirst()
                .map(AgendaEntity::getDataAgenda)
                .orElseThrow();
    }
}
