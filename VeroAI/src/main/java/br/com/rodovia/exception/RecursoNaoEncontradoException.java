package br.com.rodovia.exception;

/** Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
