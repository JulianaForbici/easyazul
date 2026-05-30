package easy.azul.api.dto.Zona;

import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalTime;

public record DadosAtualizacaoZona (
        String nome,
        BigDecimal tarifa,
        String descricao,
        Integer tempoMaximo,
        LocalTime horaInicio,
        LocalTime horaFim,
        Double latitude,
        Double longitude,
        Integer capacidade,
        Long ocupadas,
        Long disponiveis,

        @Min(0)
        Integer capacidadeVagas,

        String endereco
){}