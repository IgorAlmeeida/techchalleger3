package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ServicoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServicoMapper {

    @Mapping(source = "codigo", target = "id")
    Servico toModel(ServicoEntity entity);

    @Mapping(source = "id", target = "codigo")
    ServicoEntity toEntity(Servico model);
}
