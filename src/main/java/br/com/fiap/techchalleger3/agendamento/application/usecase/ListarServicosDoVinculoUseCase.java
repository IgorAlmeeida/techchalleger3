package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.CachePort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalVinculoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.VinculoItemDetalhadaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarServicosDoVinculoUseCase {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    private final ServicoRepositoryPort servicoPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;
    private final CachePort cachePort;

    @SuppressWarnings("unchecked")
    public List<VinculoItemDetalhadaResponse> executar(Integer vinculoId) {
        String chave = "agendamento:cache:vinculo:" + vinculoId + ":servicos";
        return cachePort.get(chave, List.class)
                .map(l -> (List<VinculoItemDetalhadaResponse>) l)
                .orElseGet(() -> {
                    List<VinculoItemDetalhadaResponse> resultado = buscarDoRepositorio(vinculoId);
                    cachePort.put(chave, resultado, TTL);
                    return resultado;
                });
    }

    private List<VinculoItemDetalhadaResponse> buscarDoRepositorio(Integer vinculoId) {
        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(vinculoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", vinculoId));

        Profissional profissional = profissionalPort.buscarPorId(vinculo.getProfissionalId()).orElseThrow();
        Estabelecimento estabelecimento = estabelecimentoPort.buscarPorId(vinculo.getEstabelecimentoId()).orElseThrow();
        ProfissionalVinculoResumo vinculoResumo = new ProfissionalVinculoResumo(
                vinculo.getId(),
                new ProfissionalResumo(profissional.getId(), profissional.getNome()),
                new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome())
        );

        List<ProfissionalVinculoServico> servicos = vinculoServicoPort.listarPorProfissionalVinculoId(vinculoId);

        return servicos.stream()
                .map(vs -> {
                    Servico servico = servicoPort.buscarPorId(vs.getServicoId())
                            .orElseThrow(() -> new RegistroNaoEncontradoException("Servico", vs.getServicoId()));
                    return new VinculoItemDetalhadaResponse(
                            vs.getId(),
                            vinculoResumo,
                            new ServicoResumo(servico.getId(), servico.getNome(), servico.getDuracaoMinutos())
                    );
                })
                .toList();
    }
}
