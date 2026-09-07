package br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler;

import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalVinculoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.VinculoItemResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.VinculoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfissionalVinculoResponseAssembler {

    private final ProfissionalRepositoryPort profissionalPort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ServicoRepositoryPort servicoPort;

    public VinculoResponse toVinculoResponse(ProfissionalVinculo v) {
        Profissional profissional = profissionalPort.buscarPorId(v.getProfissionalId()).orElseThrow();
        Estabelecimento estabelecimento = estabelecimentoPort.buscarPorId(v.getEstabelecimentoId()).orElseThrow();
        return new VinculoResponse(
                v.getId(),
                new ProfissionalResumo(profissional.getId(), profissional.getNome()),
                new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome()),
                v.getDataInicio(),
                v.getDataFim()
        );
    }

    public VinculoItemResponse toVinculoItemResponse(ProfissionalVinculoServico vi) {
        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(vi.getProfissionalVinculoId()).orElseThrow();
        Profissional profissional = profissionalPort.buscarPorId(vinculo.getProfissionalId()).orElseThrow();
        Estabelecimento estabelecimento = estabelecimentoPort.buscarPorId(vinculo.getEstabelecimentoId()).orElseThrow();
        ProfissionalVinculoResumo vinculoResumo = new ProfissionalVinculoResumo(
                vinculo.getId(),
                new ProfissionalResumo(profissional.getId(), profissional.getNome()),
                new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome())
        );
        ServicoResumo servico = servicoPort.buscarPorId(vi.getServicoId())
                .map(s -> new ServicoResumo(s.getId(), s.getNome(), s.getDuracaoMinutos()))
                .orElse(null);
        return new VinculoItemResponse(vi.getId(), vinculoResumo, servico);
    }
}
