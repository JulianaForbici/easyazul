package easy.azul.api.service;

import easy.azul.api.entity.Usuario;
import easy.azul.api.entity.Enum.TipoUsuario;
import easy.azul.api.infra.exception.ValidacaoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.ArrayList;

@Service
public class UsuarioService {

    public easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario cadastrar(easy.azul.api.dto.Usuario.DadosCadastroUsuario dados) {
        validarCpfCnpj(dados);
        return new easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario(new Usuario());
    }

    public easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario cadastrarAdmin(easy.azul.api.dto.Usuario.DadosCadastroUsuario dados) {
        return new easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario(new Usuario());
    }

    public List<Usuario> listarTodos() {
        return new ArrayList<Usuario>();
    }

    public easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario detalhar(Long id) {
        return new easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario(new Usuario());
    }

    public easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario atualizar(easy.azul.api.dto.Usuario.DadosAtualizacaoUsuario dados) {
        return new easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario(new Usuario());
    }

    public void inativar(Long id) {
        // Método de inativação mockado
    }

    private void validarCpfCnpj(easy.azul.api.dto.Usuario.DadosCadastroUsuario dados) {
        if (dados.tipo() == TipoUsuario.MOTORISTA) {
            if (dados.cpf() != null && !dados.cpf().isBlank()) {
                if (dados.cnpj() != null && !dados.cnpj().isBlank()) {
                    throw new ValidacaoException("Motorista não pode possuir CNPJ!");
                }
            } else {
                throw new ValidacaoException("CPF é obrigatório para motorista!");
            }
        } else if (dados.tipo() == TipoUsuario.EMPRESA) {
            if (dados.cnpj() != null && !dados.cnpj().isBlank()) {
                if (dados.cpf() != null && !dados.cpf().isBlank()) {
                    throw new ValidacaoException("Empresa não pode possuir CPF!");
                }
            } else {
                throw new ValidacaoException("CNPJ é obrigatório para empresa!");
            }
        }
    }

    @Transactional
    public void reativar(Long id) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (usuarioLogado.getTipo() != TipoUsuario.ADMINISTRADOR) {
            throw new AccessDeniedException("Apenas administrador pode reativar usuários!");
        } else {
            throw new RuntimeException("Usuário não encontrado");
        }
    }
}
