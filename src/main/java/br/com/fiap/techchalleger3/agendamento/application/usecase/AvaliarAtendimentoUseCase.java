package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AvaliacaoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Avaliacao;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AvaliarAtendimentoUseCase {

    private final AvaliacaoRepositoryPort avaliacaoPort;
    private final AgendamentoRepositoryPort agendamentoPort;
    private final AgendaRepositoryPort agendaPort;

    public Avaliacao avaliar(Integer agendamentoId, Integer clienteId, int nota, String comentario) {
        if (nota < 1 || nota > 5) {
            throw new OperacaoInvalidaException("Nota deve ser entre 1 e 5.");
        }

        Agendamento agendamento = agendamentoPort.buscarPorId(agendamentoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Agendamento", agendamentoId));

        if (!agendamento.getClienteId().equals(clienteId)) {
            throw new OperacaoInvalidaException("Cliente não autorizado a avaliar este agendamento.");
        }

        if (!StatusAgendamentoEnum.REALIZADO.equals(agendamento.getStatus())) {
            throw new OperacaoInvalidaException("Somente agendamentos realizados podem ser avaliados.");
        }

        if (avaliacaoPort.existePorAgendamentoId(agendamentoId)) {
            throw new OperacaoInvalidaException("Agendamento já foi avaliado.");
        }

        Agenda agenda = agendaPort.buscarPorId(agendamento.getAgendaId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Agenda", agendamento.getAgendaId()));

        Avaliacao avaliacao = Avaliacao.builder()
                .agendamentoId(agendamentoId)
                .clienteId(clienteId)
                .estabelecimentoId(agenda.getEstabelecimentoId())
                .profissionalVinculoId(agenda.getProfissionalVinculoId())
                .nota(nota)
                .comentario(comentario)
                .dhInsert(LocalDateTime.now())
                .build();

        return avaliacaoPort.salvar(avaliacao);
    }
}
