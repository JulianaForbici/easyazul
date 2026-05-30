package easy.azul.api.entity;

import easy.azul.api.entity.Enum.StatusVeiculo;
import easy.azul.api.entity.Enum.TipoVeiculo;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "veiculo")
@Entity(name = "Veiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Veiculo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario dono;

    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_veiculo", nullable = false, length = 20)
    private TipoVeiculo tipoVeiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusVeiculo status = StatusVeiculo.ATIVO;
}