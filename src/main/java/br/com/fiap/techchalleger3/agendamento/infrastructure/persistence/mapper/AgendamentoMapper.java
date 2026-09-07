package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.mapper;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.AgendamentoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgendamentoMapper {

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "codAgenda", target = "agendaId")
    @Mapping(source = "codAgendamentoPai", target = "agendamentoPaiId")
    @Mapping(source = "codServico", target = "servicoId")
    @Mapping(source = "codCliente", target = "clienteId")
    @Mapping(target = "dataAgenda", ignore = true)
    Agendamento toModel(AgendamentoEntity entity);

    @Mapping(source = "id", target = "codigo")
    @Mapping(source = "agendaId", target = "codAgenda")
    @Mapping(source = "agendamentoPaiId", target = "codAgendamentoPai")
    @Mapping(source = "servicoId", target = "codServico")
    @Mapping(source = "clienteId", target = "codCliente")
    AgendamentoEntity toEntity(Agendamento model);
}
