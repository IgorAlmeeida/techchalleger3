package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalVinculoServicoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfissionalVinculoServicoMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codProfissionalVinculo", target = "profissionalVinculoId")
    @Mapping(source = "codServico", target = "servicoId")
    ProfissionalVinculoServico toModel(ProfissionalVinculoServicoEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "profissionalVinculoId", target = "codProfissionalVinculo")
    @Mapping(source = "servicoId", target = "codServico")
    ProfissionalVinculoServicoEntity toEntity(ProfissionalVinculoServico model);
}
