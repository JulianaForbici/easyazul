package easy.azul.api.dto.Zona;

import easy.azul.api.entity.Enum.StatusZona;
import easy.azul.api.entity.ZonaEstacionamento;

import java.math.BigDecimal;
import java.time.LocalTime;

public record DadosDetalhamentoZona (
        Long id,
        String nome,
        BigDecimal tarifa,
        String descricao,
        Integer tempoMaximo,
        LocalTime horaInicio,
        LocalTime horaFim,
        Double latitude,
        Double longitude,
        Integer capacidadeVagas,
        StatusZona status
){
    public DadosDetalhamentoZona (ZonaEstacionamento zona){
        this(
                zona.getIdZonaEstacionamento(),
                zona.getNome(),
                zona.getTarifa(),
                zona.getDescricao(),
                zona.getTempoMaximo(),
                zona.getHoraInicio(),
                zona.getHoraFim(),
                zona.getLatitude(),
                zona.getLongitude(),
                zona.getCapacidadeVagas(),
                zona.getStatus());
    }
}
