package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(source = "codigo", target = "id")
    Usuario toModel(UsuarioEntity entity);

    @Mapping(source = "id", target = "codigo")
    UsuarioEntity toEntity(Usuario model);
}
