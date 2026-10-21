package br.com.rodovia.dto;

import br.com.rodovia.modelo.RelatorioPrioridade;
import java.time.LocalDateTime;

public record RelatorioResponse(
        Long idRelatorio,
        int qtUrgente,
        int qtCritico,
        int qtAtencao,
        int qtNormal,
        String resumo,
        LocalDateTime dataGeracao) {

    public static RelatorioResponse de(RelatorioPrioridade r) {
        return new RelatorioResponse(r.getIdRelatorio(), r.getQtUrgente(), r.getQtCritico(),
                r.getQtAtencao(), r.getQtNormal(), r.getResumo(), r.getDataGeracao());
    }
}
