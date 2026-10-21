package br.com.rodovia.dto;

import br.com.rodovia.modelo.MonitoravelViaIoT;
import br.com.rodovia.modelo.TrechoRodovia;

public record TrechoResponse(
        String identificador,
        double kmInicial,
        double kmFinal,
        double nivelVegetacao,
        boolean umido,
        boolean iot,
        boolean critico) {

    public static TrechoResponse de(TrechoRodovia t) {
        return new TrechoResponse(t.getIdentificador(), t.getQuilometroInicial(), t.getQuilometroFinal(),
                t.getNivelVegetacao(), t.isTrechoUmido(), t instanceof MonitoravelViaIoT, t.estaCritico());
    }
}
