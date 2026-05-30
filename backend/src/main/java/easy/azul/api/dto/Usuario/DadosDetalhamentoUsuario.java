package easy.azul.api.dto.Usuario;

import easy.azul.api.entity.Enum.StatusUsuario;
import easy.azul.api.entity.Enum.TipoUsuario;
import easy.azul.api.entity.Usuario;

import java.time.LocalDate;

public record DadosDetalhamentoUsuario(
        Long id,
        String nome,
        String email,
        String telefone,
        String cpf,
        String cnpj,
        TipoUsuario tipo,
        StatusUsuario status,
        LocalDate dataNascimento,
        String razaoSocial
) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getCpf(),
                usuario.getCnpj(),
                usuario.getTipo(),
                usuario.getStatus(),
                usuario.getDataNascimento(),
                usuario.getRazaoSocial()
        );
    }
}