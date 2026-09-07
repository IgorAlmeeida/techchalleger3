package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.converter;

import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<RoleEnum, String> {

    @Override
    public String convertToDatabaseColumn(RoleEnum role) {
        return role == null ? null : role.getCodigo();
    }

    @Override
    public RoleEnum convertToEntityAttribute(String codigo) {
        return codigo == null ? null : RoleEnum.obterPorCodigo(codigo);
    }
}
