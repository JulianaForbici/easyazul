package easy.azul.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import easy.azul.api.entity.Enum.StatusZona;

@Entity
@Table(name = "zonas_estacionamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZonaEstacionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idZonaEstacionamento;

    private String nome;
    private String descricao;
    private BigDecimal tarifa;
    private Integer capacidadeVagas;
    private Integer tempoMaximo;
    private Double latitude;
    private Double longitude;
    private LocalTime horaInicio;
    private LocalTime horaFim;

    @Enumerated(EnumType.STRING)
    private StatusZona status;

    // Métodos manuais para compatibilidade com DTOs e Services
    public java.lang.Long getIdZonaEstacionamento() {
        return this.idZonaEstacionamento;
    }

    public java.lang.String getNome() {
        return this.nome;
    }

    public java.lang.String getDescricao() {
        return this.descricao;
    }

    public java.math.BigDecimal getTarifa() {
        return this.tarifa;
    }

    public java.lang.Integer getCapacidadeVagas() {
        return this.capacidadeVagas;
    }

    public java.lang.Integer getTempoMaximo() {
        return this.tempoMaximo;
    }

    public java.lang.Double getLatitude() {
        return this.latitude;
    }

    public java.lang.Double getLongitude() {
        return this.longitude;
    }

    public java.time.LocalTime getHoraInicio() {
        return this.horaInicio;
    }

    public java.time.LocalTime getHoraFim() {
        return this.horaFim;
    }

    public StatusZona getStatus() {
        return this.status;
    }

    public void setNome(java.lang.String nome) {
        this.nome = nome;
    }

    public void setDescricao(java.lang.String descricao) {
        this.descricao = descricao;
    }

    public void setTarifa(java.math.BigDecimal tarifa) {
        this.tarifa = tarifa;
    }

    public void setCapacidadeVagas(java.lang.Integer capacidadeVagas) {
        this.capacidadeVagas = capacidadeVagas;
    }

    public void setTempoMaximo(java.lang.Integer tempoMaximo) {
        this.tempoMaximo = tempoMaximo;
    }

    public void setLatitude(java.lang.Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(java.lang.Double longitude) {
        this.longitude = longitude;
    }

    public void setHoraInicio(java.time.LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public void setHoraFim(java.time.LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    public void setStatus(StatusZona status) {
        this.status = status;
    }
}
