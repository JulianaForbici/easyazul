package easy.azul.api.dto.Zona;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalTime;

public record DadosCadastroZona (
        @NotBlank
        String nome,

        String endereco,

        String descricao,

        Double latitude,
        Double longitude,

        @NotNull
        @Digits(integer = 6, fraction = 2)
        BigDecimal tarifa,

        @NotNull
        @Positive
        Integer tempoMaximo,

        @NotNull
        LocalTime horaInicio,

        @NotNull
        LocalTime horaFim,

        @NotNull
        @Min(0)
        Integer capacidadeVagas
) {}