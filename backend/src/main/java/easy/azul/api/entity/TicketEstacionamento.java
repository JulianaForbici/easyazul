package easy.azul.api.entity;
import easy.azul.api.entity.Veiculo;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import easy.azul.api.entity.Enum.StatusTicket;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketEstacionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaEstacionamento zona;

    private LocalDateTime inicioTicket;
    private LocalDateTime venceEm;
    private LocalDateTime fimTicket;
    private BigDecimal valor;
    private boolean ativo;

    @Enumerated(EnumType.STRING)
    private StatusTicket status;

    public easy.azul.api.entity.Veiculo getVeiculo() {
        return this.veiculo;
    }

    public easy.azul.api.entity.ZonaEstacionamento getZona() {
        return this.zona;
    }

    public java.math.BigDecimal getValor() {
        return this.valor;
    }

    public boolean getAtivo() {
        return this.ativo;
    }

    public StatusTicket getStatus() {
        return this.status;
    }

    public void setStatus(StatusTicket status) {
        this.status = status;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public void setInicioTicket(LocalDateTime inicioTicket) {
        this.inicioTicket = inicioTicket;
    }

    public void setFimTicket(LocalDateTime fimTicket) {
        this.fimTicket = fimTicket;
    }
    // Método que mapeia a chamada de getIdTicket() para a variável 'id' da entidade
    public java.lang.Long getIdTicket() {
        return this.id;
    }

    public java.time.LocalDateTime getInicioTicket() {
        return this.inicioTicket;
    }

    public java.time.LocalDateTime getVenceEm() {
        return this.venceEm;
    }

    public java.time.LocalDateTime getFimTicket() {
        return this.fimTicket;
    }

        public void setValor(java.math.BigDecimal valor) {
        this.valor = valor;
    }

    public void setVenceEm(java.time.LocalDateTime venceEm) {
        this.venceEm = venceEm;
    }
    public void setVeiculo(easy.azul.api.entity.Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public void setZona(easy.azul.api.entity.ZonaEstacionamento zona) {
        this.zona = zona;
    }

}
