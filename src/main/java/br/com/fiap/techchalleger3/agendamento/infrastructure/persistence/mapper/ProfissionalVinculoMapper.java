package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalVinculoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfissionalVinculoMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codProfissional", target = "profissionalId")
    @Mapping(source = "codEstabelecimento", target = "estabelecimentoId")
    ProfissionalVinculo toModel(ProfissionalVinculoEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "profissionalId", target = "codProfissional")
    @Mapping(source = "estabelecimentoId", target = "codEstabelecimento")
    ProfissionalVinculoEntity toEntity(ProfissionalVinculo model);
}
