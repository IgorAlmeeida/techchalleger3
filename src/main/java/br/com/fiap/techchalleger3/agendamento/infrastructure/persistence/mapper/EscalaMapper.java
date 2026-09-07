package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EscalaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EscalaMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codProfissionalVinculo", target = "profissionalVinculoId")
    @Mapping(source = "codEstabelecimento", target = "estabelecimentoId")
    Escala toModel(EscalaEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "profissionalVinculoId", target = "codProfissionalVinculo")
    @Mapping(source = "estabelecimentoId", target = "codEstabelecimento")
    EscalaEntity toEntity(Escala model);
}
