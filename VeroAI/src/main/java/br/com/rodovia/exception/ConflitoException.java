package br.com.rodovia.exception;

/** Duplicidade ou registro com dependentes. Vira HTTP 409. */
public class ConflitoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
