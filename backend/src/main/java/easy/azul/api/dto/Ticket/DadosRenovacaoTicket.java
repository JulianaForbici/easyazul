package easy.azul.api.dto.Ticket;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DadosRenovacaoTicket(
    @NotNull(message = "A quantidade de minutos adicionais é obrigatória")
    @Positive(message = "Os minutos adicionais devem ser maiores que zero")
    Long minutosAdicionais
) {
}