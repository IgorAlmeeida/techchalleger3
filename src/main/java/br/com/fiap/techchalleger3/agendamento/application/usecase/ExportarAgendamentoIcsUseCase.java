package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.CalendarioExportPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExportarAgendamentoIcsUseCase {

    private final AgendamentoRepositoryPort agendamentoPort;
    private final CalendarioExportPort calendarioExportPort;

    public byte[] exportar(Integer agendamentoId) {
        Agendamento agendamento = agendamentoPort.buscarPorId(agendamentoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Agendamento", agendamentoId));
        return calendarioExportPort.exportarIcs(agendamento);
    }
}
