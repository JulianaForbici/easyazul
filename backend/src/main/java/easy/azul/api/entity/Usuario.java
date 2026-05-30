//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package easy.azul.api.entity;

import easy.azul.api.dto.Usuario.DadosAtualizacaoUsuario;
import easy.azul.api.dto.Usuario.DadosCadastroUsuario;
import easy.azul.api.entity.Enum.StatusUsuario;
import easy.azul.api.entity.Enum.TipoUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.Generated;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Table(
        name = "usuario"
)
@Entity(
        name = "Usuario"
)
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id_usuario"
    )
    private Long idUsuario;
    private String nome;
    @Column(
            nullable = false,
            unique = true
    )
    private String email;
    private String senha;
    @Column(
            unique = true
    )
    private String cpf;
    @Column(
            unique = true
    )
    private String cnpj;
    @Column(
            nullable = false,
            unique = true
    )
    private String telefone;
    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;
    @Enumerated(EnumType.STRING)
    private StatusUsuario status;
    @Column(
            name = "data_nascimento"
    )
    private LocalDate dataNascimento;
    @Column(
            name = "razao_social"
    )
    private String razaoSocial;

    public Usuario(DadosCadastroUsuario dados) {
        this.nome = dados.nome();
        this.email = dados.email();
        this.senha = dados.senha();
        this.cpf = dados.cpf();
        this.cnpj = dados.cnpj();
        this.telefone = dados.telefone();
        this.tipo = dados.tipo();
        this.dataNascimento = dados.dataNascimento();
        this.razaoSocial = dados.razaoSocial();
        this.status = StatusUsuario.ATIVO;
    }

    public void inativar() {
        this.status = StatusUsuario.INATIVO;
    }

    public void atualizarInformacoes(DadosAtualizacaoUsuario dados) {
        if (dados.nome() != null && !dados.nome().isBlank()) {
            this.nome = dados.nome();
        }

        if (dados.email() != null && !dados.email().isBlank()) {
            this.email = dados.email();
        }

        if (dados.telefone() != null && !dados.telefone().isBlank()) {
            this.telefone = dados.telefone();
        }

        if (dados.cnpj() != null && !dados.cnpj().isBlank()) {
            this.cnpj = dados.cnpj();
        }

        if (dados.cpf() != null && !dados.cpf().isBlank()) {
            this.cpf = dados.cpf();
        }

    }

    public void reativar() {
        this.status = StatusUsuario.ATIVO;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.tipo.name()));
    }

    public String getPassword() {
        return this.senha;
    }

    public String getUsername() {
        return this.email;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return this.status == StatusUsuario.ATIVO;
    }

    @Generated
    public Long getIdUsuario() {
        return this.idUsuario;
    }

    @Generated
    public String getNome() {
        return this.nome;
    }

    @Generated
    public String getEmail() {
        return this.email;
    }

    @Generated
    public String getSenha() {
        return this.senha;
    }

    @Generated
    public String getCpf() {
        return this.cpf;
    }

    @Generated
    public String getCnpj() {
        return this.cnpj;
    }

    @Generated
    public String getTelefone() {
        return this.telefone;
    }

    @Generated
    public TipoUsuario getTipo() {
        return this.tipo;
    }

    @Generated
    public StatusUsuario getStatus() {
        return this.status;
    }

    @Generated
    public LocalDate getDataNascimento() {
        return this.dataNascimento;
    }

    @Generated
    public String getRazaoSocial() {
        return this.razaoSocial;
    }

    @Generated
    public void setIdUsuario(final Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Generated
    public void setNome(final String nome) {
        this.nome = nome;
    }

    @Generated
    public void setEmail(final String email) {
        this.email = email;
    }

    @Generated
    public void setSenha(final String senha) {
        this.senha = senha;
    }

    @Generated
    public void setCpf(final String cpf) {
        this.cpf = cpf;
    }

    @Generated
    public void setCnpj(final String cnpj) {
        this.cnpj = cnpj;
    }

    @Generated
    public void setTelefone(final String telefone) {
        this.telefone = telefone;
    }

    @Generated
    public void setTipo(final TipoUsuario tipo) {
        this.tipo = tipo;
    }

    @Generated
    public void setStatus(final StatusUsuario status) {
        this.status = status;
    }

    @Generated
    public void setDataNascimento(final LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    @Generated
    public void setRazaoSocial(final String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    @Generated
    public Usuario() {
    }

    @Generated
    public Usuario(final Long idUsuario, final String nome, final String email, final String senha, final String cpf, final String cnpj, final String telefone, final TipoUsuario tipo, final StatusUsuario status, final LocalDate dataNascimento, final String razaoSocial) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cpf = cpf;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.tipo = tipo;
        this.status = status;
        this.dataNascimento = dataNascimento;
        this.razaoSocial = razaoSocial;
    }

    @Generated
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Usuario)) {
            return false;
        } else {
            Usuario other = (Usuario)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$idUsuario = this.getIdUsuario();
                Object other$idUsuario = other.getIdUsuario();
                if (this$idUsuario == null) {
                    if (other$idUsuario != null) {
                        return false;
                    }
                } else if (!this$idUsuario.equals(other$idUsuario)) {
                    return false;
                }

                return true;
            }
        }
    }

    @Generated
    protected boolean canEqual(final Object other) {
        return other instanceof Usuario;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $idUsuario = this.getIdUsuario();
        result = result * 59 + ($idUsuario == null ? 43 : $idUsuario.hashCode());
        return result;
    }
}