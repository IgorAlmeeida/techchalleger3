package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.ProfissionalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProfissionalMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codUsuario", target = "usuarioId")
    @Mapping(source = "especialidades", target = "especialidades")
    Profissional toModel(ProfissionalEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "usuarioId", target = "codUsuario")
    @Mapping(source = "especialidades", target = "especialidades")
    ProfissionalEntity toEntity(Profissional model);

    default List<String> stringToList(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    default String listToString(List<String> value) {
        if (value == null || value.isEmpty()) return null;
        return String.join(",", value);
    }
}
