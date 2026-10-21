package br.com.rodovia.exception;

/** Violacao de regra de negocio / dado invalido. Vira HTTP 400. */
public class RegraNegocioException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
