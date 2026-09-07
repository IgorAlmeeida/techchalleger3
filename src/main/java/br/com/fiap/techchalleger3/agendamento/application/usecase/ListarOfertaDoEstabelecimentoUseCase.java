package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalDisponivelResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalVinculoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ListarOfertaDoEstabelecimentoUseCase {

    private final EstabelecimentoRepositoryPort estabelecimentoPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    private final ServicoRepositoryPort servicoPort;
    private final ProfissionalRepositoryPort profissionalPort;

    public List<ProfissionalDisponivelResponse> executar(Integer estabelecimentoId, Integer servicoIdFiltro) {
        Estabelecimento estabelecimento = estabelecimentoPort.buscarPorId(estabelecimentoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Estabelecimento", estabelecimentoId));

        EstabelecimentoResumo estabelecimentoResumo = new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome());

        List<Integer> vinculoIds = profissionalVinculoPort.listarIdsPorEstabelecimento(estabelecimentoId);

        return vinculoIds.stream()
                .map(id -> profissionalVinculoPort.buscarPorId(id).orElse(null))
                .filter(Objects::nonNull)
                .map(vinculo -> toResponse(vinculo, estabelecimentoResumo, servicoIdFiltro))
                .filter(Objects::nonNull)
                .toList();
    }

    private ProfissionalDisponivelResponse toResponse(ProfissionalVinculo vinculo, EstabelecimentoResumo estabelecimentoResumo,
                                                       Integer servicoFiltro) {
        List<ServicoResumo> servicos = vinculoServicoPort.listarPorProfissionalVinculoId(vinculo.getId())
                .stream()
                .filter(vs -> servicoFiltro == null || vs.getServicoId().equals(servicoFiltro))
                .map(vs -> servicoPort.buscarPorId(vs.getServicoId())
                        .map(s -> new ServicoResumo(s.getId(), s.getNome(), s.getDuracaoMinutos()))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        if (servicos.isEmpty()) return null;

        Profissional profissional = profissionalPort.buscarPorId(vinculo.getProfissionalId()).orElseThrow();
        ProfissionalVinculoResumo vinculoResumo = new ProfissionalVinculoResumo(
                vinculo.getId(),
                new ProfissionalResumo(profissional.getId(), profissional.getNome()),
                estabelecimentoResumo
        );
        return new ProfissionalDisponivelResponse(vinculoResumo, servicos);
    }
}
