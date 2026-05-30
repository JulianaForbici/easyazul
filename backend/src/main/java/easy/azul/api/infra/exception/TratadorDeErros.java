package easy.azul.api.infra.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    private record ErroApi(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            List<DadosErroValidacao> fields
    ) {}

    private record DadosErroValidacao(String campo, String mensagem) {
        public DadosErroValidacao(FieldError erro) {
            this(erro.getField(), garantirExclamacao(erro.getDefaultMessage()));
        }
    }

    private static String garantirExclamacao(String msg) {
        if (msg == null || msg.isBlank()) return "Erro inesperado!";
        String t = msg.trim();
        return t.endsWith("!") ? t : (t + "!");
    }

    private ResponseEntity<ErroApi> resposta(HttpStatus status, String message, String path, List<DadosErroValidacao> fields) {
        return ResponseEntity.status(status).body(
                new ErroApi(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        garantirExclamacao(message),
                        path,
                        fields == null ? List.of() : fields));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroApi> tratar404(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApi> tratar400Validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var fields = ex.getFieldErrors().stream().map(DadosErroValidacao::new).toList();
        return resposta(HttpStatus.BAD_REQUEST, "Erro de validação!", req.getRequestURI(), fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroApi> tratarConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<DadosErroValidacao> fields = ex.getConstraintViolations().stream()
                .map(this::toFieldViolation)
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Erro de validação!", req.getRequestURI(), fields);
    }

    private DadosErroValidacao toFieldViolation(ConstraintViolation<?> v) {
        String campo = v.getPropertyPath() == null ? "parametro" : v.getPropertyPath().toString();
        return new DadosErroValidacao(campo, garantirExclamacao(v.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroApi> tratar400Json(HttpMessageNotReadableException ex, HttpServletRequest req) {
        Throwable root = rootCause(ex);

        if (root instanceof InvalidFormatException ife) {
            String campo = ife.getPath() != null && !ife.getPath().isEmpty()
                    ? ife.getPath().get(ife.getPath().size() - 1).getFieldName()
                    : "campo";

            String msg = "Valor inválido para o campo '" + campo + "'!";
            return resposta(HttpStatus.BAD_REQUEST, msg, req.getRequestURI(),
                    List.of(new DadosErroValidacao(campo, msg)));
        }

        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida! JSON malformado ou valor inválido!", req.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroApi> tratarTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String campo = ex.getName() == null ? "parametro" : ex.getName();
        String msg = "Valor inválido para o parâmetro '" + campo + "'!";
        return resposta(HttpStatus.BAD_REQUEST, msg, req.getRequestURI(),
                List.of(new DadosErroValidacao(campo, msg)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroApi> tratarFaltaParametro(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String campo = ex.getParameterName();
        String msg = "Parâmetro obrigatório '" + campo + "' ausente!";
        return resposta(HttpStatus.BAD_REQUEST, msg, req.getRequestURI(),
                List.of(new DadosErroValidacao(campo, msg)));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroApi> tratarFaltaHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        String campo = ex.getHeaderName();
        String msg = "Header obrigatório '" + campo + "' ausente!";
        return resposta(HttpStatus.BAD_REQUEST, msg, req.getRequestURI(),
                List.of(new DadosErroValidacao(campo, msg)));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroApi> tratar405(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado para este endpoint!", req.getRequestURI(), List.of());
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErroApi> tratarRegraNegocio(ValidacaoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, garantirExclamacao(ex.getMessage()), req.getRequestURI(), List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroApi> tratarBadCredentials(HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, "Credenciais inválidas!", req.getRequestURI(), List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroApi> tratarAuthentication(HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, "Falha na autenticação!", req.getRequestURI(), List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroApi> tratarAcessoNegado(HttpServletRequest req) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado!", req.getRequestURI(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroApi> tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        String raw = String.valueOf(ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        String m = raw.toLowerCase(Locale.ROOT);

        if (m.contains("duplicate key value violates unique constraint")) {
            String constraint = extrairConstraint(raw);
            var info = mapearUniqueConstraint(constraint);

            String msg = info.mensagem();
            List<DadosErroValidacao> fields = info.campo() == null
                    ? List.of()
                    : List.of(new DadosErroValidacao(info.campo(), msg));

            return resposta(HttpStatus.CONFLICT, msg, req.getRequestURI(), fields);
        }

        if (m.contains("violates not-null constraint")) {
            String coluna = extrairColunaNotNull(raw);
            String msg = coluna != null
                    ? "Campo obrigatório '" + coluna + "' ausente!"
                    : "Campo obrigatório ausente!";

            List<DadosErroValidacao> fields = coluna != null
                    ? List.of(new DadosErroValidacao(coluna, msg))
                    : List.of();

            return resposta(HttpStatus.BAD_REQUEST, msg, req.getRequestURI(), fields);
        }

        if (m.contains("violates foreign key constraint")) {
            return resposta(
                    HttpStatus.CONFLICT,
                    "Não foi possível concluir a operação! Existe relacionamento com outro registro!",
                    req.getRequestURI(),
                    List.of());
        }

        if (m.contains("violates check constraint")) {
            return resposta(
                    HttpStatus.BAD_REQUEST,
                    "Valor inválido!",
                    req.getRequestURI(),
                    List.of());
        }

        return resposta(
                HttpStatus.CONFLICT,
                "Violação de integridade no banco!",
                req.getRequestURI(),
                List.of());
    }

    private record UniqueInfo(String campo, String mensagem) {}

    private UniqueInfo mapearUniqueConstraint(String constraintName) {
        String c = (constraintName == null ? "" : constraintName.toLowerCase(Locale.ROOT));

        if (c.contains("usuario_cpf_key")) return new UniqueInfo("cpf", "CPF já cadastrado!");
        if (c.contains("usuario_cnpj_key")) return new UniqueInfo("cnpj", "CNPJ já cadastrado!");
        if (c.contains("usuario_telefone_key")) return new UniqueInfo("telefone", "Telefone já cadastrado!");
        if (c.contains("usuario_email_key")) return new UniqueInfo("email", "Email já cadastrado!");
        if (c.contains("veiculo_placa_key")) return new UniqueInfo("placa", "Placa já cadastrada!");
        if (c.contains("zona_estacionamento_nome_key")) return new UniqueInfo("nome", "Já existe uma zona com esse nome!");

        return new UniqueInfo(null, "Registro duplicado!");
    }

    private String extrairConstraint(String raw) {
        Pattern p = Pattern.compile("constraint \"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(raw);
        return m.find() ? m.group(1) : null;
    }

    private String extrairColunaNotNull(String raw) {
        Pattern p = Pattern.compile("null value in column \"([^\"]+)\" violates not-null constraint", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(raw);
        return m.find() ? m.group(1) : null;
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroApi> tratar500(Exception ex, HttpServletRequest req) {
        log.error("Erro interno na API! path={}", req.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor!", req.getRequestURI(), List.of());
    }
}