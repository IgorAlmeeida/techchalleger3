package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendaEmAbertoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Encerra o vínculo de um profissional com um estabelecimento, definindo data de fim como hoje.
 * Bloqueia o encerramento se existirem agendas futuras abertas para o vínculo.
 */
@Service
@RequiredArgsConstructor
public class DesvincularProfissionalUseCase {

    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final AgendaRepositoryPort agendaPort;

    @Transactional
    public void executar(Integer vinculoId) {
        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(vinculoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", vinculoId));

        if (agendaPort.existeAgendaFuturaPorVinculo(vinculoId)) {
            throw new AgendaEmAbertoException("o vínculo " + vinculoId);
        }

        vinculo.setDataFim(LocalDate.now());
        profissionalVinculoPort.salvar(vinculo);
    }
}
