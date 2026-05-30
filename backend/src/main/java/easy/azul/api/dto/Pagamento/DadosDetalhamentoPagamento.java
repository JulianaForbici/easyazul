package easy.azul.api.dto.Pagamento;

import easy.azul.api.entity.Enum.FormaPagamento;
import easy.azul.api.entity.Enum.StatusPagamento;
import easy.azul.api.entity.Pagamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DadosDetalhamentoPagamento(
        Long idPagamento,
        Long idTicket,
        BigDecimal valor,
        FormaPagamento metodo,
        StatusPagamento status,
        String observacao,
        LocalDate dataPagamento,
        String codigoReferencia
) {
    public DadosDetalhamentoPagamento(Pagamento pagamento) {
        this(
                pagamento.getIdPagamento(),
                pagamento.getTicket() != null ? pagamento.getTicket().getIdTicket() : null,
                pagamento.getValor(),
                pagamento.getFormaPagamento(),
                pagamento.getStatus(),
                pagamento.getObservacao(),
                pagamento.getDataPagamento(),
                pagamento.getCodigoReferencia()
        );
    }
}