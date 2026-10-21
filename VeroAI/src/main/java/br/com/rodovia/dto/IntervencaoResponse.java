package br.com.rodovia.dto;

import br.com.rodovia.modelo.IntervencaoOperacional;
import java.time.LocalDateTime;

public record IntervencaoResponse(
        Long idIntervencao,
        String tipoIntervencao,
        Long idEquipe,
        String nomeEquipe,
        String idTrecho,
        LocalDateTime dataRegistro) {

    public static IntervencaoResponse de(IntervencaoOperacional i) {
        return new IntervencaoResponse(i.getIdIntervencao(), i.getTipo().name(),
                i.getEquipe().getIdEquipe(), i.getEquipe().getNomeEquipe(),
                i.getTrecho().getIdentificador(), i.getDataRegistro());
    }
}
