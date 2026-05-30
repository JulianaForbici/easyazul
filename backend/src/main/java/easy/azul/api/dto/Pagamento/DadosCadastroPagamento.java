package easy.azul.api.dto.Pagamento;

import easy.azul.api.entity.Enum.FormaPagamento;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroPagamento(
        @NotNull
        Long idTicket,

        @NotNull
        FormaPagamento formaPagamento,

        String codigoReferencia,
        String observacao
) {}