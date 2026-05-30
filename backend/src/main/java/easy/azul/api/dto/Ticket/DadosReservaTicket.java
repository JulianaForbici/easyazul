package easy.azul.api.dto.Ticket;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public record DadosReservaTicket(
        @JsonAlias({"idZona"})
        @NotNull
        Long idZonaEstacionamento,
        @NotNull
        Long idVeiculo
) {}