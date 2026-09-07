package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EscalaItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EscalaItemMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codEscala", target = "escalaId")
    @Mapping(source = "codServico", target = "servicoId")
    EscalaItem toModel(EscalaItemEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "escalaId", target = "codEscala")
    @Mapping(source = "servicoId", target = "codServico")
    EscalaItemEntity toEntity(EscalaItem model);
}
