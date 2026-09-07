package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.CachePort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendaEmAbertoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesvincularItemUseCase {

    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    private final AgendaRepositoryPort agendaPort;
    private final AgendaItemRepositoryPort agendaItemPort;
    private final CachePort cachePort;

    @Transactional
    public void executar(Integer vinculoId, Integer servicoId) {
        profissionalVinculoPort.buscarPorId(vinculoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", vinculoId));

        if (!vinculoServicoPort.existePorVinculoEServico(vinculoId, servicoId)) {
            throw new RegistroNaoEncontradoException("ProfissionalVinculoServico", vinculoId + "/" + servicoId);
        }

        List<Agenda> agendasFuturas = agendaPort.listarFuturasPorVinculo(vinculoId);
        if (!agendasFuturas.isEmpty()) {
            List<Integer> agendaIds = agendasFuturas.stream().map(Agenda::getId).toList();
            if (agendaItemPort.existePorAgendaIdsEServico(agendaIds, servicoId)) {
                throw new AgendaEmAbertoException("o serviço " + servicoId + " no vínculo " + vinculoId);
            }
        }

        vinculoServicoPort.deletarPorVinculoEServico(vinculoId, servicoId);
        cachePort.invalidar("agendamento:cache:vinculo:" + vinculoId + ":servicos");
    }
}
