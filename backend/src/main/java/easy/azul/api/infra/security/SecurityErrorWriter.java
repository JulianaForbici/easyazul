package easy.azul.api.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class SecurityErrorWriter {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public record ErroApi(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            List<Object> fields) {}

    private static String garantirExclamacao(String msg) {
        if (msg == null || msg.isBlank()) return "Erro inesperado!";
        String t = msg.trim();
        return t.endsWith("!") ? t : (t + "!");
    }

    public static void write(HttpServletRequest req, HttpServletResponse res,
                             HttpStatus status, String message) throws IOException {

        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);

        var body = new ErroApi(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                garantirExclamacao(message),
                req.getRequestURI(),
                List.of());

        mapper.writeValue(res.getOutputStream(), body);
    }
}