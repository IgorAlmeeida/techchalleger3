package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.AgendaItem;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendaItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgendaItemMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codAgenda", target = "agendaId")
    @Mapping(source = "codServico", target = "servicoId")
    AgendaItem toModel(AgendaItemEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "agendaId", target = "codAgenda")
    @Mapping(source = "servicoId", target = "codServico")
    AgendaItemEntity toEntity(AgendaItem model);
}
