package easy.azul.api.dto.Pagamento;

import jakarta.validation.constraints.NotBlank;

public record DadosCancelamentoPagamento(
        @NotBlank
        String motivo
) {}