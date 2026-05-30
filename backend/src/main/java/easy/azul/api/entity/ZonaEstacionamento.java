package easy.azul.api.entity;

import easy.azul.api.entity.Enum.StatusZona;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Table(name = "zona_estacionamento")
@Entity(name = "ZonaEstacionamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idZonaEstacionamento")
public class ZonaEstacionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zonaestacionamento")
    private Long idZonaEstacionamento;

    @Column(nullable = false, length = 250)
    private String nome;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal tarifa;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "tempo_maximo", nullable = false)
    private Integer tempoMaximo;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusZona status = StatusZona.ATIVA;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "capacidade_vagas", nullable = false)
    private Integer capacidadeVagas;
}