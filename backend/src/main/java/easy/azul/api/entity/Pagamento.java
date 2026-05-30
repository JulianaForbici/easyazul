package easy.azul.api.entity;

import easy.azul.api.entity.Enum.FormaPagamento;
import easy.azul.api.entity.Enum.StatusPagamento;
import easy.azul.api.entity.Enum.TipoReferenciaPagamento;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "pagamento")
@Entity(name = "Pagamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idPagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    private Long idPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket", nullable = false)
    private TicketEstacionamento ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "valor", nullable = false, precision = 8, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Column(name = "codigo_referencia", length = 80)
    private String codigoReferencia;

    @Column(name = "observacao", length = 255)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_referencia", nullable = false, length = 20)
    private TipoReferenciaPagamento tipoReferencia;
}