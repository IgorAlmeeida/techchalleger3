package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ClienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codUsuario", target = "usuarioId")
    Cliente toModel(ClienteEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "usuarioId", target = "codUsuario")
    ClienteEntity toEntity(Cliente model);
}
