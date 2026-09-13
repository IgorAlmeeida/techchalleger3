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
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.HorarioDisponivelResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResumo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public List<HorarioDisponivelResponse> executar(Integer profissionalVinculoId, Integer servicoId) {
        if (profissionalVinculoId == null) {
            throw new OperacaoInvalidaException("profissionalVinculoId é obrigatório.");
        }
        if (servicoId == null) {
            throw new OperacaoInvalidaException("servicoId é obrigatório.");
        }

        Servico servico = servicoPort.buscarPorId(servicoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Servico", servicoId));
        int nSlots = servico.getDuracaoMinutos() / 5;

        List<Agendamento> disponiveis = agendamentoPort.buscarDisponiveisPorVinculo(profissionalVinculoId);
        Map<Integer, List<Agendamento>> porAgenda = BuscadorDeJanelaDeSlots.agrupar(disponiveis);

        Map<Integer, List<Agendamento>> porAgendaComServico = porAgenda.entrySet().stream()
                .filter(e -> agendaItemPort.existePorAgendaIdsEServico(List.of(e.getKey()), servicoId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        List<Agendamento> inicios = BuscadorDeJanelaDeSlots.buscarIniciosDeJanelasNaoSobrepostas(porAgendaComServico, nSlots);

        return inicios.stream()
                .map(slot -> toResponse(slot, servico))
                .toList();
    }

    private HorarioDisponivelResponse toResponse(Agendamento agendamento, Servico servico) {
        Agenda agenda = agendaPort.buscarPorId(agendamento.getAgendaId()).orElseThrow();
        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(agenda.getProfissionalVinculoId()).orElseThrow();
        Profissional profissional = profissionalPort.buscarPorId(vinculo.getProfissionalId()).orElseThrow();
        Estabelecimento estabelecimento = estabelecimentoPort.buscarPorId(vinculo.getEstabelecimentoId()).orElseThrow();

        return new HorarioDisponivelResponse(
                agenda.getId(),
                agendamento.getId(),
                agenda.getDataAgenda(),
                agendamento.getHoraInicio(),
                agendamento.getHoraInicio().plusMinutes(servico.getDuracaoMinutos()),
                new ProfissionalResumo(profissional.getId(), profissional.getNome()),
                new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome())
        );
    }
}
