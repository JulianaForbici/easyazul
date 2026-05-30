package easy.azul.api.dto.Usuario;

import easy.azul.api.infra.validation.cnpj.CNPJ;
import easy.azul.api.infra.validation.cpf.CPF;
import easy.azul.api.entity.Enum.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DadosCadastroUsuario(

        @NotBlank
        String nome,

        @NotBlank
        @Email(message = "Email inválido")
        String email,

        @NotBlank
        @Size(min = 6)
        String senha,

        @CPF
        String cpf,

        @CNPJ
        String cnpj,

        @NotBlank
        @Pattern(regexp = "^\\(?\\d{2}\\)?[ ]?9?\\d{4}-?\\d{4}$", message = "Telefone inválido")
        String telefone,

        TipoUsuario tipo,
        LocalDate dataNascimento,
        String razaoSocial
) {}