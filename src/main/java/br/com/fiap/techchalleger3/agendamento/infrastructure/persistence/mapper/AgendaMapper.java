package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgendaMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codEscala", target = "escalaId")
    @Mapping(source = "codEstabelecimento", target = "estabelecimentoId")
    @Mapping(source = "codProfissionalVinculo", target = "profissionalVinculoId")
    Agenda toModel(AgendaEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "escalaId", target = "codEscala")
    @Mapping(source = "estabelecimentoId", target = "codEstabelecimento")
    @Mapping(source = "profissionalVinculoId", target = "codProfissionalVinculo")
    AgendaEntity toEntity(Agenda model);
}
