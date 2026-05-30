package easy.azul.api.service;

import easy.azul.api.dto.Veiculo.DadosAtualizacaoVeiculo;
import easy.azul.api.dto.Veiculo.DadosCadastroVeiculo;
import easy.azul.api.dto.Veiculo.DadosDetalhamentoVeiculo;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.Enum.StatusUsuario;
import easy.azul.api.entity.Enum.StatusVeiculo;
import easy.azul.api.entity.Enum.TipoUsuario;
import easy.azul.api.entity.Usuario;
import easy.azul.api.entity.Veiculo;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.UsuarioRepository;
import easy.azul.api.repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("veiculoService")
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TicketEstacionamentoRepository ticketEstacionamentoRepository;

    @Transactional
    public DadosDetalhamentoVeiculo cadastrar(DadosCadastroVeiculo dados) {

        Usuario usuarioLogado = (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (usuarioLogado.getTipo() != TipoUsuario.ADMINISTRADOR) {
            if (!dados.idDono().equals(usuarioLogado.getIdUsuario())) {
                throw new ValidacaoException("Você não pode cadastrar veículo para outro usuário.");
            }
        }

        String placaCorreta = dados.placa().trim().toUpperCase();

        if (veiculoRepository.existsByPlaca(placaCorreta)) {
            throw new ValidacaoException("Já há um veículo cadastrado com essa placa.");
        }

        Usuario dono = usuarioRepository.getReferenceById(dados.idDono());

        if (dono.getStatus() != StatusUsuario.ATIVO) {
            throw new ValidacaoException("Usuário inativo não pode ter veículo.");
        }

        if (dono.getTipo() != TipoUsuario.MOTORISTA
                && dono.getTipo() != TipoUsuario.EMPRESA
                && dono.getTipo() != TipoUsuario.FISCAL) {
            throw new ValidacaoException("Administrador não pode ter veículo");
        }

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(placaCorreta);
        veiculo.setDono(dono);
        veiculo.setTipoVeiculo(dados.tipoVeiculo());
        veiculo.setStatus(StatusVeiculo.ATIVO);

        return new DadosDetalhamentoVeiculo(veiculoRepository.save(veiculo));
    }

    @Transactional
    public DadosDetalhamentoVeiculo atualizar(Long id, DadosAtualizacaoVeiculo dados) {

        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ValidacaoException("Veículo não encontrado"));

        if (veiculo.getStatus() == StatusVeiculo.INATIVO) {
            throw new ValidacaoException("Veículo inativo não pode ser atualizado.");
        }

        Usuario usuarioLogado = (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (usuarioLogado.getTipo() != TipoUsuario.ADMINISTRADOR &&
                !veiculo.getDono().getIdUsuario().equals(usuarioLogado.getIdUsuario())) {
            throw new AccessDeniedException("Você não tem permissão para essa ação.");
        }

        if (dados.placa() != null && !dados.placa().isBlank()) {
            String placa = dados.placa().trim().toUpperCase();

            if (!placa.equals(veiculo.getPlaca()) && veiculoRepository.existsByPlaca(placa)) {
                throw new ValidacaoException("Placa já cadastrada.");
            }

            veiculo.setPlaca(placa);
        }

        if (dados.tipoVeiculo() != null) {
            veiculo.setTipoVeiculo(dados.tipoVeiculo());
        }

        return new DadosDetalhamentoVeiculo(veiculo);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoVeiculo> listarDoUsuarioLogado(Pageable pageable) {

        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return veiculoRepository
                .findByDonoIdUsuarioAndStatus(usuario.getIdUsuario(), StatusVeiculo.ATIVO, pageable)
                .map(DadosDetalhamentoVeiculo::new);
    }

    @Transactional(readOnly = true)
    public boolean ehDonoEmpresaOuAdmin(Long id) {

        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (usuario.getTipo() == TipoUsuario.ADMINISTRADOR) return true;

        Veiculo v = veiculoRepository.findById(id)
                .orElseThrow(() -> new ValidacaoException("Veículo não encontrado"));

        if (v.getStatus() == StatusVeiculo.INATIVO) return false;

        return v.getDono().getIdUsuario().equals(usuario.getIdUsuario());
    }

    @Transactional
    public void excluir(Long id) {

        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ValidacaoException("Veículo não encontrado"));

        if (veiculo.getStatus() == StatusVeiculo.INATIVO) {
            throw new ValidacaoException("Veículo já está inativo");
        }

        boolean temAtivo = ticketEstacionamentoRepository
                .existsByVeiculo_IdAndStatus(id, StatusTicket.ATIVO);

        if (temAtivo) {
            throw new ValidacaoException("Não é possível excluir. Há ticket ATIVO para esse veículo");
        }

        veiculo.setStatus(StatusVeiculo.INATIVO);
    }
}