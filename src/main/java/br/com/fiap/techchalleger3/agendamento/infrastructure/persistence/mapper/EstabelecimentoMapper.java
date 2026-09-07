package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.EstabelecimentoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EstabelecimentoMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "fotosUrls", target = "fotosUrls")
    Estabelecimento toModel(EstabelecimentoEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "fotosUrls", target = "fotosUrls")
    EstabelecimentoEntity toEntity(Estabelecimento model);

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
