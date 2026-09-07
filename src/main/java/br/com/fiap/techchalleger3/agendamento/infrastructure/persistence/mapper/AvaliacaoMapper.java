package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Avaliacao;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AvaliacaoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvaliacaoMapper {

    @Mapping(source = "codAvaliacao", target = "id")
    @Mapping(source = "codAgendamento", target = "agendamentoId")
    @Mapping(source = "codCliente", target = "clienteId")
    @Mapping(source = "codEstabelecimento", target = "estabelecimentoId")
    @Mapping(source = "codProfissionalVinculo", target = "profissionalVinculoId")
    Avaliacao toModel(AvaliacaoEntity entity);

    @Mapping(source = "id", target = "codAvaliacao")
    @Mapping(source = "agendamentoId", target = "codAgendamento")
    @Mapping(source = "clienteId", target = "codCliente")
    @Mapping(source = "estabelecimentoId", target = "codEstabelecimento")
    @Mapping(source = "profissionalVinculoId", target = "codProfissionalVinculo")
    AvaliacaoEntity toEntity(Avaliacao model);
}
