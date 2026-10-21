package br.com.rodovia.modelo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanNumericConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean valor) {
        return Boolean.TRUE.equals(valor) ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer valor) {
        return valor != null && valor == 1;
    }
}
