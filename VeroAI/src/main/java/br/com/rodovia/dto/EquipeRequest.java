package br.com.rodovia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipeRequest(
        @NotBlank(message = "e obrigatorio") @Size(max = 100, message = "deve ter no maximo 100 caracteres")
        String nomeEquipe) {
}
