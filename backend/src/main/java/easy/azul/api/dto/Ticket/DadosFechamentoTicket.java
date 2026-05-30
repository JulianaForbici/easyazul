package easy.azul.api.dto.Ticket;

import jakarta.validation.constraints.NotNull;

public record DadosFechamentoTicket(
        @NotNull
        Long idTicket
) {}