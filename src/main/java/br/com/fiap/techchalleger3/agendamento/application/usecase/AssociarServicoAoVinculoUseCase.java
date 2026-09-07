package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.CachePort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssociarServicoAoVinculoUseCase {

    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ServicoRepositoryPort servicoPort;
    private final ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    private final CachePort cachePort;

    @Transactional
    public ProfissionalVinculoServico executar(Integer profissionalVinculoId, Integer servicoId) {
        profissionalVinculoPort.buscarPorId(profissionalVinculoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", profissionalVinculoId));

        servicoPort.buscarPorId(servicoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Servico", servicoId));

        if (vinculoServicoPort.existePorVinculoEServico(profissionalVinculoId, servicoId)) {
            throw new OperacaoInvalidaException("Este serviço já está associado ao vínculo.");
        }

        ProfissionalVinculoServico salvo = vinculoServicoPort.salvar(ProfissionalVinculoServico.builder()
                .profissionalVinculoId(profissionalVinculoId)
                .servicoId(servicoId)
                .build());

        cachePort.invalidar("agendamento:cache:vinculo:" + profissionalVinculoId + ":servicos");
        return salvo;
    }
}
