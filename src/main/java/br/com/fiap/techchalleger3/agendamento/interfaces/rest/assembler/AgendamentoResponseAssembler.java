package br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler;

import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AgendamentoResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ClienteResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgendamentoResponseAssembler {

    private final ServicoRepositoryPort servicoPort;
    private final ClienteRepositoryPort clientePort;

    public AgendamentoResponse toResponse(Agendamento a) {
        ServicoResumo servico = a.getServicoId() == null ? null :
                servicoPort.buscarPorId(a.getServicoId())
                        .map(s -> new ServicoResumo(s.getId(), s.getNome(), s.getDuracaoMinutos()))
                        .orElse(null);
        ClienteResumo cliente = a.getClienteId() == null ? null :
                clientePort.buscarPorId(a.getClienteId())
                        .map(c -> new ClienteResumo(c.getId(), c.getNome()))
                        .orElse(null);
        return new AgendamentoResponse(a.getId(), a.getAgendaId(), a.getDataAgenda(), servico,
                a.getHoraInicio(), a.getHoraFim(), cliente, a.getStatus(), a.getPresencaConfirmada());
    }
}
