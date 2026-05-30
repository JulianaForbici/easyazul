//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package easy.azul.api.service;

import easy.azul.api.dto.Usuario.DadosAtualizacaoUsuario;
import easy.azul.api.dto.Usuario.DadosCadastroUsuario;
import easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario;
import easy.azul.api.entity.Usuario;
import easy.azul.api.entity.Enum.StatusUsuario;
import easy.azul.api.entity.Enum.TipoUsuario;
import easy.azul.api.infra.exception.RecursoNaoEncontradoException;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public DadosDetalhamentoUsuario cadastrar(DadosCadastroUsuario dados) {
        if (dados.tipo() != TipoUsuario.ADMINISTRADOR && dados.tipo() != TipoUsuario.FISCAL) {
            this.validarDuplicidadeEmailTelefone(dados.email(), dados.telefone(), (Usuario)null);
            this.validarCpfCnpjPorTipo(dados);
            String cpfNormalizado = this.normalizarCpf(dados.cpf());
            if (cpfNormalizado != null && this.usuarioRepository.existsByCpf(cpfNormalizado)) {
                throw new ValidacaoException("CPF já cadastrado!");
            } else {
                String cnpjNormalizado = this.normalizarCnpj(dados.cnpj());
                if (cnpjNormalizado != null && this.usuarioRepository.existsByCnpj(cnpjNormalizado)) {
                    throw new ValidacaoException("CNPJ já cadastrado!");
                } else {
                    Usuario usuario = new Usuario(dados);
                    usuario.setCpf(cpfNormalizado);
                    usuario.setCnpj(cnpjNormalizado);
                    usuario.setSenha(this.passwordEncoder.encode(dados.senha()));
                    Usuario usuarioSalvo = (Usuario)this.usuarioRepository.save(usuario);
                    return new DadosDetalhamentoUsuario(usuarioSalvo);
                }
            }
        } else {
            throw new ValidacaoException("Tipo de usuário não permitido para cadastro público!");
        }
    }

    @Transactional
    public DadosDetalhamentoUsuario cadastrarAdmin(DadosCadastroUsuario dados) {
        this.validarDuplicidadeEmailTelefone(dados.email(), dados.telefone(), (Usuario)null);
        this.validarCpfCnpjPorTipo(dados);
        if (dados.tipo() == TipoUsuario.ADMINISTRADOR || dados.tipo() == TipoUsuario.FISCAL) {
            if (dados.cpf() == null || dados.cpf().isBlank()) {
                throw new ValidacaoException("CPF é obrigatório para fiscal/administrador!");
            }

            if (dados.cnpj() != null && !dados.cnpj().isBlank()) {
                throw new ValidacaoException("Fiscal e administrador não podem possuir CNPJ!");
            }
        }

        String cpfNormalizado = this.normalizarCpf(dados.cpf());
        if (cpfNormalizado != null && this.usuarioRepository.existsByCpf(cpfNormalizado)) {
            throw new ValidacaoException("CPF já cadastrado!");
        } else {
            String cnpjNormalizado = this.normalizarCnpj(dados.cnpj());
            if (cnpjNormalizado != null && this.usuarioRepository.existsByCnpj(cnpjNormalizado)) {
                throw new ValidacaoException("CNPJ já cadastrado!");
            } else {
                Usuario usuario = new Usuario(dados);
                usuario.setCpf(cpfNormalizado);
                usuario.setCnpj(cnpjNormalizado);
                usuario.setSenha(this.passwordEncoder.encode(dados.senha()));
                return new DadosDetalhamentoUsuario((Usuario)this.usuarioRepository.save(usuario));
            }
        }
    }

    @Transactional
    public DadosDetalhamentoUsuario atualizar(DadosAtualizacaoUsuario dados) {

        Usuario usuario = usuarioRepository.findById(dados.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado!"));

        validarDuplicidadeEmailTelefone(dados.email(), dados.telefone(), usuario);

        String cpfAtual = usuario.getCpf();
        String cnpjAtual = usuario.getCnpj();
        String senhaHashAtual = usuario.getSenha();

        String cpfNorm = normalizarCpf(dados.cpf());
        String cnpjNorm = normalizarCnpj(dados.cnpj());

        boolean enviouCpf = cpfNorm != null && !cpfNorm.isBlank();
        boolean enviouCnpj = cnpjNorm != null && !cnpjNorm.isBlank();
        boolean enviouSenha = dados.senha() != null && !dados.senha().isBlank();

        if (enviouCpf && enviouCnpj) {
            throw new ValidacaoException("Informe apenas CPF ou CNPJ, não ambos.");
        }

        if (enviouCpf && usuarioRepository.existsByCpf(cpfNorm)
                && (cpfAtual == null || !cpfNorm.equals(cpfAtual))) {
            throw new ValidacaoException("CPF já cadastrado!");
        }

        if (enviouCnpj && usuarioRepository.existsByCnpj(cnpjNorm)
                && (cnpjAtual == null || !cnpjNorm.equals(cnpjAtual))) {
            throw new ValidacaoException("CNPJ já cadastrado!");
        }

        usuario.atualizarInformacoes(dados);

        if (!enviouSenha) {
            usuario.setSenha(senhaHashAtual);
        } else {
            usuario.setSenha(passwordEncoder.encode(dados.senha()));
        }

        if (!enviouCpf && !enviouCnpj) {
            usuario.setCpf(cpfAtual);
            usuario.setCnpj(cnpjAtual);
        } else if (enviouCpf) {
            usuario.setCpf(cpfNorm);
            usuario.setCnpj(null);
        } else { // enviouCnpj
            usuario.setCnpj(cnpjNorm);
            usuario.setCpf(null);
        }

        usuarioRepository.save(usuario);
        return new DadosDetalhamentoUsuario(usuario);
    }

    @Transactional(
            readOnly = true
    )
    public DadosDetalhamentoUsuario detalhar(Long id) {
        Usuario usuario = (Usuario)this.usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado!"));
        return new DadosDetalhamentoUsuario(usuario);
    }

    @Transactional(
            readOnly = true
    )
    public Page<DadosDetalhamentoUsuario> listarAtivos(Pageable paginacao) {
        return this.usuarioRepository.findAllByStatus(StatusUsuario.ATIVO, paginacao).map(DadosDetalhamentoUsuario::new);
    }

    @Transactional
    public void inativar(Long id) {
        Usuario usuarioLogado = (Usuario)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario alvo = (Usuario)this.usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado!"));
        boolean isAdmin = usuarioLogado.getTipo() == TipoUsuario.ADMINISTRADOR;
        if (!isAdmin && !usuarioLogado.getIdUsuario().equals(id)) {
            throw new AccessDeniedException("Você não pode inativar outro usuário!");
        } else if (alvo.getTipo() == TipoUsuario.ADMINISTRADOR) {
            throw new ValidacaoException("Não é permitido inativar um administrador!");
        } else {
            alvo.inativar();
            this.usuarioRepository.save(alvo);
        }
    }

    @Transactional(
            readOnly = true
    )
    public boolean proprioUsuarioOuAdministrador(Long idUsuario) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = (Usuario)auth.getPrincipal();
        return usuarioLogado.getIdUsuario().equals(idUsuario) || usuarioLogado.getTipo() == TipoUsuario.ADMINISTRADOR;
    }

    private void validarDuplicidadeEmailTelefone(String email, String telefone, Usuario usuarioAtual) {
        if (email != null && !email.isBlank()) {
            boolean mudouEmail = usuarioAtual == null || !email.equalsIgnoreCase(usuarioAtual.getEmail());
            if (mudouEmail && this.usuarioRepository.existsByEmail(email)) {
                throw new ValidacaoException("E-mail já cadastrado!");
            }
        }

        if (telefone != null && !telefone.isBlank()) {
            boolean mudouTelefone = usuarioAtual == null || !telefone.equals(usuarioAtual.getTelefone());
            if (mudouTelefone && this.usuarioRepository.existsByTelefone(telefone)) {
                throw new ValidacaoException("Telefone já cadastrado!");
            }
        }

    }

    private void validarCpfCnpjPorTipo(DadosCadastroUsuario dados) {
        if (dados.tipo() != TipoUsuario.MOTORISTA || dados.cpf() != null && !dados.cpf().isBlank()) {
            if (dados.tipo() != TipoUsuario.EMPRESA || dados.cnpj() != null && !dados.cnpj().isBlank()) {
                if (dados.tipo() == TipoUsuario.EMPRESA && dados.cpf() != null && !dados.cpf().isBlank()) {
                    throw new ValidacaoException("Empresa não pode possuir CPF!");
                } else if (dados.tipo() != TipoUsuario.EMPRESA && dados.cnpj() != null && !dados.cnpj().isBlank()) {
                    throw new ValidacaoException("Apenas empresa pode possuir CNPJ!");
                }
            } else {
                throw new ValidacaoException("CNPJ é obrigatório para empresa!");
            }
        } else {
            throw new ValidacaoException("CPF é obrigatório para motorista!");
        }
    }

    private String normalizarCpf(String cpf) {
        return cpf != null && !cpf.isBlank() ? cpf.replaceAll("\\D", "") : null;
    }

    private String normalizarCnpj(String cnpj) {
        return cnpj != null && !cnpj.isBlank() ? cnpj.replaceAll("\\D", "") : null;
    }

    @Transactional
    public void reativar(Long id) {
        Usuario usuarioLogado = (Usuario)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (usuarioLogado.getTipo() != TipoUsuario.ADMINISTRADOR) {
            throw new AccessDeniedException("Apenas administrador pode reativar usuários!");
        } else {
            Usuario alvo = (Usuario)this.usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado!"));
            alvo.reativar();
            this.usuarioRepository.save(alvo);
        }
    }
}
