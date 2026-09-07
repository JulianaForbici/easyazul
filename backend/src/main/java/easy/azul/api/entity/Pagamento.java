package easy.azul.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import easy.azul.api.entity.Enum.FormaPagamento;
import easy.azul.api.entity.Enum.StatusPagamento;
import easy.azul.api.entity.Enum.TipoReferenciaPagamento;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private TicketEstacionamento ticket;

    private BigDecimal valor;
    
    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    @Enumerated(EnumType.STRING)
    private TipoReferenciaPagamento tipoReferencia;

    private String observacao;
    private String codigoReferencia;
    private LocalDate dataPagamento;

    // Métodos manuais para garantir compatibilidade
    public java.lang.Long getIdPagamento() {
        return this.idPagamento;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public TicketEstacionamento getTicket() {
        return this.ticket;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public FormaPagamento getFormaPagamento() {
        return this.formaPagamento;
    }

    public StatusPagamento getStatus() {
        return this.status;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public String getCodigoReferencia() {
        return this.codigoReferencia;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public void setTipoReferencia(TipoReferenciaPagamento tipoReferencia) {
        this.tipoReferencia = tipoReferencia;
    }

    public void setCodigoReferencia(String codigoReferencia) {
        this.codigoReferencia = codigoReferencia;
    }

    // Retorna LocalDate puro para matar o erro de tipo incompatível do DTO
    public LocalDate getDataPagamento() {
        return this.dataPagamento;
    }
    public void setTicket(easy.azul.api.entity.TicketEstacionamento ticket) {
        this.ticket = ticket;
    }

    public void setUsuario(easy.azul.api.entity.Usuario usuario) {
        this.usuario = usuario;
    }

    public void setValor(java.math.BigDecimal valor) {
        this.valor = valor;
    }

}
