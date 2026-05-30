package easy.azul.api.entity;

import easy.azul.api.entity.Enum.StatusTicket;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "ticket_estacionamento")
@Entity(name = "TicketEstacionamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idTicket")
public class TicketEstacionamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket")
    private Long idTicket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_veiculo")
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_zonaestacionamento")
    private ZonaEstacionamento zona;

    @Column(name = "inicio_ticket", nullable = false)
    private LocalDateTime inicioTicket;

    @Column(name = "fim_ticket")
    private LocalDateTime fimTicket;

    @Column(name = "valor", nullable = false, precision = 8, scale = 2)
    private BigDecimal valor;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTicket status;
}