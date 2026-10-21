package br.com.rodovia.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TrechoRequest(
        @Size(max = 50, message = "deve ter no maximo 50 caracteres") String identificador,
        @NotNull(message = "e obrigatorio") @PositiveOrZero(message = "nao pode ser negativo")
        @DecimalMax(value = "9999.99", message = "maximo 9999.99") Double kmInicial,
        @NotNull(message = "e obrigatorio") @PositiveOrZero(message = "nao pode ser negativo")
        @DecimalMax(value = "9999.99", message = "maximo 9999.99") Double kmFinal,
        @NotNull(message = "e obrigatorio") @PositiveOrZero(message = "nao pode ser negativo")
        @DecimalMax(value = "9999.99", message = "maximo 9999.99") Double nivelVegetacao,
        @NotNull(message = "e obrigatorio") Boolean umido,
        Boolean iot) {
}
