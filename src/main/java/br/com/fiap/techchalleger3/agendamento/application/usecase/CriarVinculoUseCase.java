package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CriarVinculoUseCase {

    private final ProfissionalRepositoryPort profissionalPort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;

    @Transactional
    public ProfissionalVinculo executar(Integer profissionalId, Integer estabelecimentoId, LocalDate dataInicio) {
        profissionalPort.buscarPorId(profissionalId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional", profissionalId));

        estabelecimentoPort.buscarPorId(estabelecimentoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Estabelecimento", estabelecimentoId));

        if (profissionalVinculoPort.existeVinculoAtivo(profissionalId, estabelecimentoId)) {
            throw new OperacaoInvalidaException("Já existe um vínculo ativo entre este profissional e este estabelecimento.");
        }

        return profissionalVinculoPort.salvar(ProfissionalVinculo.builder()
                .profissionalId(profissionalId)
                .estabelecimentoId(estabelecimentoId)
                .dataInicio(dataInicio)
                .dhInsert(LocalDateTime.now(ZoneId.systemDefault()))
                .build());
    }
}
