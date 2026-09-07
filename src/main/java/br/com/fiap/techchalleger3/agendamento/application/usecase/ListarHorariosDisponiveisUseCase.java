package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.domain.service.BuscadorDeJanelaDeSlots;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarHorariosDisponiveisUseCase {

    private final AgendamentoRepositoryPort agendamentoPort;
    private final AgendaRepositoryPort agendaPort;
    private final AgendaItemRepositoryPort agendaItemPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;
    private final ServicoRepositoryPort servicoPort;

    public List<HorarioDisponivel> executar(Integer profissionalVinculoId, Integer servicoId) {
        if (profissionalVinculoId == null) {
            throw new OperacaoInvalidaException("profissionalVinculoId é obrigatório.");
        }
        if (servicoId == null) {
            throw new OperacaoInvalidaException("servicoId é obrigatório para busca de janelas disponíveis.");
        }

        Servico servico = servicoPort.buscarPorId(servicoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Servico", servicoId));
        int n = servico.getDuracaoMinutos() / 5;

        List<Agendamento> disponiveis = agendamentoPort.buscarDisponiveisPorVinculo(profissionalVinculoId);

        Map<Integer, List<Agendamento>> porAgenda = disponiveis.stream()
                .collect(Collectors.groupingBy(
                        Agendamento::getAgendaId,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(Agendamento::getHoraInicio))
                                        .toList())));

        Map<Integer, List<Agendamento>> porAgendaComServico = porAgenda.entrySet().stream()
                .filter(e -> agendaItemPort.existePorAgendaIdsEServico(List.of(e.getKey()), servicoId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        List<Agendamento> inicios = BuscadorDeJanelaDeSlots.buscarIniciosDeJanelas(porAgendaComServico, n);

        return inicios.stream().map(inicio -> enriquecer(inicio, servico)).toList();
    }

    private HorarioDisponivel enriquecer(Agendamento inicio, Servico servico) {
        Agenda agenda = agendaPort.buscarPorId(inicio.getAgendaId()).orElseThrow();
        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(agenda.getProfissionalVinculoId()).orElseThrow();
        Profissional profissional = profissionalPort.buscarPorId(vinculo.getProfissionalId()).orElseThrow();
        Estabelecimento estabelecimento = estabelecimentoPort.buscarPorId(vinculo.getEstabelecimentoId()).orElseThrow();

        return HorarioDisponivel.builder()
                .agendaId(agenda.getId())
                .agendamentoId(inicio.getId())
                .dataAgenda(agenda.getDataAgenda())
                .horaInicio(inicio.getHoraInicio())
                .horaFim(inicio.getHoraInicio().plusMinutes(servico.getDuracaoMinutos()))
                .profissional(new ProfissionalResumo(profissional.getId(), profissional.getNome()))
                .estabelecimento(new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome()))
                .servico(new ServicoResumo(servico.getId(), servico.getNome(), servico.getDuracaoMinutos()))
                .build();
    }

    @Getter
    @Builder
    public static class HorarioDisponivel {
        private final Integer agendaId;
        private final Integer agendamentoId;
        private final LocalDate dataAgenda;
        private final LocalTime horaInicio;
        private final LocalTime horaFim;
        private final ProfissionalResumo profissional;
        private final EstabelecimentoResumo estabelecimento;
        private final ServicoResumo servico;
    }
}
