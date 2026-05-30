package easy.azul.api.infra.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String msg) {
        super(msg);
    }
}