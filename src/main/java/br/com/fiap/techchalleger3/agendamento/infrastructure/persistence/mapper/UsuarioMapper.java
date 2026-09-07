package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codKeycloak", target = "keycloakId")
    Usuario toModel(UsuarioEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "keycloakId", target = "codKeycloak")
    UsuarioEntity toEntity(Usuario model);
}
