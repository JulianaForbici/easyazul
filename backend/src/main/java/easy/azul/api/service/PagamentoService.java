package easy.azul.api.service;

import easy.azul.api.dto.Pagamento.DadosCadastroPagamento;
import easy.azul.api.dto.Pagamento.DadosDetalhamentoPagamento;
import easy.azul.api.infra.exception.RecursoNaoEncontradoException;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.entity.Enum.StatusPagamento;
import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.Enum.TipoReferenciaPagamento;
import easy.azul.api.entity.Enum.TipoUsuario;
import easy.azul.api.entity.Pagamento;
import easy.azul.api.entity.TicketEstacionamento;
import easy.azul.api.entity.Usuario;
import easy.azul.api.repository.PagamentoRepository;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private TicketEstacionamentoRepository ticketRepository;

    @Autowired
    private Clock clock;

    @Transactional
    public DadosDetalhamentoPagamento criar(DadosCadastroPagamento dados) {

        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TicketEstacionamento ticket = ticketRepository.findById(dados.idTicket())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ticket não encontrado!"));

        if (ticket.getStatus() != StatusTicket.FECHADO) {
            throw new ValidacaoException("Só é possível pagar ticket FECHADO!");
        }

        if (!ticket.getVeiculo().getDono().getIdUsuario().equals(usuarioLogado.getIdUsuario())) {
            throw new AccessDeniedException("Você não pode pagar ticket de outro usuário!");
        }

        boolean existePagamento = pagamentoRepository.existsByTicket_IdTicketAndStatusIn(
                ticket.getIdTicket(),
                List.of(StatusPagamento.PENDENTE, StatusPagamento.APROVADO));

        if (existePagamento) {
            throw new ValidacaoException("Já existe pagamento para este ticket!");
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setTicket(ticket);
        pagamento.setUsuario(usuarioLogado);
        pagamento.setValor(ticket.getValor());
        pagamento.setFormaPagamento(dados.formaPagamento());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setDataPagamento(LocalDate.now(clock));
        pagamento.setTipoReferencia(TipoReferenciaPagamento.TICKET);
        pagamento.setCodigoReferencia(dados.codigoReferencia());
        pagamento.setObservacao(dados.observacao());

        return new DadosDetalhamentoPagamento(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public DadosDetalhamentoPagamento confirmar(Long idPagamento) {

        Pagamento pagamento = pagamentoRepository.findById(idPagamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado!"));

        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new ValidacaoException("Somente pagamentos PENDENTES podem ser confirmados!");
        }

        if (pagamento.getTicket().getStatus() != StatusTicket.FECHADO) {
            throw new ValidacaoException("Somente tickets FECHADOS podem ser confirmados!");
        }

        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setObservacao("Pagamento confirmado!");

        return new DadosDetalhamentoPagamento(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public DadosDetalhamentoPagamento cancelar(Long idPagamento, String motivo) {

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Pagamento pagamento = pagamentoRepository.findById(idPagamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado!"));

        boolean isAdmin = usuario.getTipo() == TipoUsuario.ADMINISTRADOR;

        if (!isAdmin) {
            if (!pagamento.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new AccessDeniedException("Você não pode cancelar pagamento de outro usuário!");
            }
            if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
                throw new ValidacaoException("Você só pode cancelar pagamentos PENDENTES!");
            }
        }

        if (pagamento.getStatus() == StatusPagamento.CANCELADO) {
            throw new ValidacaoException("Pagamento já está CANCELADO!");
        }

        if (isAdmin) {
            if (motivo == null || motivo.isBlank()) {
                throw new ValidacaoException("Motivo é obrigatório para cancelamento pelo Administrador!");
            }
            pagamento.setObservacao("Cancelado pelo Administrador: " + motivo.trim());
        } else {
            pagamento.setObservacao("Pagamento cancelado pelo usuário!");
        }

        pagamento.setStatus(StatusPagamento.CANCELADO);
        return new DadosDetalhamentoPagamento(pagamentoRepository.save(pagamento));
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoPagamento> listar(Long ticketId, Pageable pageable) {

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (usuario.getTipo() == TipoUsuario.ADMINISTRADOR || usuario.getTipo() == TipoUsuario.FISCAL) {

            if (ticketId == null) {
                return pagamentoRepository.findAll(pageable).map(DadosDetalhamentoPagamento::new);
            }

            return pagamentoRepository.findByTicket_IdTicket(ticketId, pageable)
                    .map(DadosDetalhamentoPagamento::new);
        }

        return pagamentoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario(), pageable).map(DadosDetalhamentoPagamento::new);
    }

    @Transactional(readOnly = true)
    public boolean podeCancelar(Long idPagamento) {

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Pagamento pagamento = pagamentoRepository.findById(idPagamento)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado!"));

        if (usuario.getTipo() == TipoUsuario.ADMINISTRADOR) {
            return true;
        }

        return pagamento.getStatus() == StatusPagamento.PENDENTE && pagamento.getUsuario().getIdUsuario().equals(usuario.getIdUsuario());
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoPagamento> listarMeus(Pageable pageable) {

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return pagamentoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario(), pageable).map(DadosDetalhamentoPagamento::new);
    }
}