package easy.azul.api.dto.Usuario;

import easy.azul.api.infra.validation.cnpj.CNPJ;
import easy.azul.api.infra.validation.cpf.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record DadosAtualizacaoUsuario(
        Long id,
        String nome,
        String senha,

        @Email(message = "E-mail inválido!")
        String email,

        @Pattern(regexp = "^\\(?\\d{2}\\)?[ ]?9?\\d{4}-?\\d{4}$", message = "Telefone inválido")
        String telefone,

        @CNPJ
        String cnpj,

        @CPF
        String cpf
) {}