package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.converter;

import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DiaSemanaConverter implements AttributeConverter<DiaSemanaEnum, String> {

    @Override
    public String convertToDatabaseColumn(DiaSemanaEnum dia) {
        return dia == null ? null : dia.getCodigo();
    }

    @Override
    public DiaSemanaEnum convertToEntityAttribute(String codigo) {
        return codigo == null ? null : DiaSemanaEnum.obterPorCodigo(codigo);
    }
}
