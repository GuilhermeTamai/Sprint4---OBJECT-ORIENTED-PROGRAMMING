package br.com.rodovia.dto;

import br.com.rodovia.modelo.EquipeManutencao;

public record EquipeResponse(Long idEquipe, String nomeEquipe) {

    public static EquipeResponse de(EquipeManutencao e) {
        return new EquipeResponse(e.getIdEquipe(), e.getNomeEquipe());
    }
}
