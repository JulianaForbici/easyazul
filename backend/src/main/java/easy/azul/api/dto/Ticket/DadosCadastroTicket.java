package easy.azul.api.dto.Ticket;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroTicket(
        @NotNull
        Long idVeiculo,
        @NotNull
        Long idZona
) {}