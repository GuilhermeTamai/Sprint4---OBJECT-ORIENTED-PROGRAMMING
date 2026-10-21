package br.com.rodovia.exception;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Tratamento global de erros: traduz excecoes em codigos HTTP + JSON padronizado. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    /** Falhas do Bean Validation (@Valid) nos DTOs de entrada. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados de entrada invalidos.", detalhes);
    }

    /** JSON malformado, tipo errado, parametro ausente ou com formato invalido. */
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErroResponse> requisicaoInvalida(Exception ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Requisicao invalida: verifique o JSON enviado e os parametros da URL.", List.of());
    }

    /** Rede de seguranca para violacoes de constraint (FK, UNIQUE) vindas do Oracle. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT,
                "Operacao viola uma restricao de integridade do banco (registro duplicado ou com dependentes).",
                List.of());
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem, List<String> detalhes) {
        ErroResponse corpo = new ErroResponse(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), mensagem, detalhes);
        return ResponseEntity.status(status).body(corpo);
    }
}
