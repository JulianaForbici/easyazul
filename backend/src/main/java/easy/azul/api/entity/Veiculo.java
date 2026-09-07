package easy.azul.api.entity;

import jakarta.persistence.*;
import lombok.*;
import easy.azul.api.entity.Enum.TipoVeiculo;
import easy.azul.api.entity.Enum.StatusVeiculo;

@Entity
@Table(name = "veiculos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario dono;

    @Enumerated(EnumType.STRING)
    private TipoVeiculo tipoVeiculo;

    @Enumerated(EnumType.STRING)
    private StatusVeiculo status;

    public java.lang.Long getId() {
        return this.id;
    }

    public java.lang.String getPlaca() {
        return this.placa;
    }

    public Usuario getDono() {
        return this.dono;
    }

    public TipoVeiculo getTipoVeiculo() {
        return this.tipoVeiculo;
    }

    public StatusVeiculo getStatus() {
        return this.status;
    }

    public void setPlaca(java.lang.String placa) {
        this.placa = placa;
    }

    public void setDono(Usuario dono) {
        this.dono = dono;
    }

    public void setTipoVeiculo(TipoVeiculo tipoVeiculo) {
        this.tipoVeiculo = tipoVeiculo;
    }

    public void setStatus(StatusVeiculo status) {
        this.status = status;
    }
}
