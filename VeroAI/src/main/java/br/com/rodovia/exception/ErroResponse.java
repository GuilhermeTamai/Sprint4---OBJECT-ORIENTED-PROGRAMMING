package br.com.rodovia.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Corpo padrao de todas as respostas de erro da API. */
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes) {

    public static ErroResponse de(int status, String erro, String mensagem) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, List.of());
    }
}
