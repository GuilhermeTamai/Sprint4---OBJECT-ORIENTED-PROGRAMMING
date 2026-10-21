package br.com.rodovia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IntervencaoRequest(
        @NotBlank(message = "e obrigatorio (ROCADA_MECANIZADA ou PULVERIZACAO)") String tipoIntervencao,
        @NotNull(message = "e obrigatorio") @Positive(message = "deve ser positivo") Long idEquipe,
        @NotBlank(message = "e obrigatorio") String idTrecho) {
}
