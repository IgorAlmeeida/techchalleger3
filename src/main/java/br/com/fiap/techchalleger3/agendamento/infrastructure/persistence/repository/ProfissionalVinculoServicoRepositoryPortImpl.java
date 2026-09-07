package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper.ProfissionalVinculoServicoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProfissionalVinculoServicoRepositoryPortImpl implements ProfissionalVinculoServicoRepositoryPort {

    private final ProfissionalVinculoServicoRepository repository;
    private final ProfissionalVinculoServicoMapper mapper;

    @Override
    public List<ProfissionalVinculoServico> listarPorProfissionalVinculoId(Integer profissionalVinculoId) {
        return repository.findByCodProfissionalVinculo(profissionalVinculoId)
                .stream().map(mapper::toModel).toList();
    }

    @Override
    public boolean existePorVinculoEServico(Integer profissionalVinculoId, Integer servicoId) {
        return repository.existsByCodProfissionalVinculoAndCodServico(profissionalVinculoId, servicoId);
    }

    @Override
    public void deletarPorVinculoEServico(Integer profissionalVinculoId, Integer servicoId) {
        repository.deleteByCodProfissionalVinculoAndCodServico(profissionalVinculoId, servicoId);
    }

    @Override
    public ProfissionalVinculoServico salvar(ProfissionalVinculoServico vinculoServico) {
        return mapper.toModel(repository.save(mapper.toEntity(vinculoServico)));
    }
}
