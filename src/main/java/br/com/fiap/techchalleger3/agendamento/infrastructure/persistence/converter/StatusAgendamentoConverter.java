package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.converter;

import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusAgendamentoConverter implements AttributeConverter<StatusAgendamentoEnum, String> {

    @Override
    public String convertToDatabaseColumn(StatusAgendamentoEnum status) {
        return status == null ? null : status.getCodigo();
    }

    @Override
    public StatusAgendamentoEnum convertToEntityAttribute(String codigo) {
        return codigo == null ? null : StatusAgendamentoEnum.obterPorCodigo(codigo);
    }
}
