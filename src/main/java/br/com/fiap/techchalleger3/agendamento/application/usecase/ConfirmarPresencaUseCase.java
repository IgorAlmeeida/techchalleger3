package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConfirmarPresencaUseCase {

    private final AgendamentoRepositoryPort agendamentoPort;
    private final AgendaRepositoryPort agendaPort;

    @Transactional
    public Agendamento executar(Integer agendamentoId) {
        Agendamento agendamento = agendamentoPort.buscarPorId(agendamentoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Agendamento", agendamentoId));

        if (agendamento.getAgendamentoPaiId() != null) {
            throw new OperacaoInvalidaException("Confirme a presença usando o ID do agendamento pai.");
        }

        if (!StatusAgendamentoEnum.AGENDADO.equals(agendamento.getStatus())) {
            throw new OperacaoInvalidaException("Confirmação de presença só é permitida em agendamentos com status AGENDADO.");
        }

        LocalDateTime agora = LocalDateTime.now();
        agendamento.setPresencaConfirmada(true);
        agendamento.setDhAtualizacao(agora);
        Agendamento salvo = agendamentoPort.salvar(agendamento);
        agendaPort.buscarPorId(salvo.getAgendaId())
                .ifPresent(a -> salvo.setDataAgenda(a.getDataAgenda()));

        for (Agendamento filho : agendamentoPort.buscarFilhosPorPaiId(agendamentoId)) {
            filho.setPresencaConfirmada(true);
            filho.setDhAtualizacao(agora);
            agendamentoPort.salvar(filho);
        }

        return salvo;
    }
}
