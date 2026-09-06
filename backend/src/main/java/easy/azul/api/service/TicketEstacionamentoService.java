package easy.azul.api.service;

import easy.azul.api.dto.Ticket.DadosCadastroTicket;
import easy.azul.api.dto.Ticket.DadosDetalhamentoTicket;
import easy.azul.api.dto.Ticket.DadosReservaTicket;
import easy.azul.api.dto.Ticket.DadosRenovacaoTicket;
import easy.azul.api.entity.Enum.*;
import easy.azul.api.infra.exception.RecursoNaoEncontradoException;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.entity.TicketEstacionamento;
import easy.azul.api.entity.Usuario;
import easy.azul.api.entity.Veiculo;
import easy.azul.api.entity.ZonaEstacionamento;
import easy.azul.api.repository.PagamentoRepository;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.VeiculoRepository;
import easy.azul.api.repository.ZonaEstacionamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service("ticketEstacionamentoService")
public class TicketEstacionamentoService {

    @Autowired
    private TicketEstacionamentoRepository ticketRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ZonaEstacionamentoRepository zonaRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private Clock clock;

    public boolean podeIniciar(Long id) {
        return true;
    }

    public boolean podeRenovar(Long id) {
        return podeFechar(id);
    }

    @Transactional
    public DadosDetalhamentoTicket abrir(DadosCadastroTicket dados) {

        Veiculo veiculo = veiculoRepository.findById(dados.idVeiculo())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado!"));

        ZonaEstacionamento zona = zonaRepository.findById(dados.idZona())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Zona não encontrada!"));

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        validarStatusVeiculo(veiculo);
        validarFechadoSemPagamento(veiculo);
        validarStatusZona(zona);
        validarHorarioZona(zona, LocalTime.now(clock));

        if (usuario.getTipo() == TipoUsuario.MOTORISTA || usuario.getTipo() == TipoUsuario.EMPRESA) {
            if (!veiculo.getDono().getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new AccessDeniedException("Você não pode abrir ticket para veículo de outro usuário!");
            }
        }

        boolean existeAtivo = ticketRepository.existsByVeiculo_IdAndStatus(veiculo.getId(), StatusTicket.ATIVO);
        if (existeAtivo) {
            throw new ValidacaoException("Já existe um ticket ativo para este veículo!");
        }

        TicketEstacionamento ticket = new TicketEstacionamento();
        ticket.setVeiculo(veiculo);
        ticket.setZona(zona);
        LocalDateTime inicioTicket = LocalDateTime.now(clock);
        ticket.setInicioTicket(inicioTicket);
        ticket.setVenceEm(inicioTicket.plusMinutes(zona.getTempoMaximo()));
        ticket.setFimTicket(null);
        ticket.setAtivo(true);
        ticket.setStatus(StatusTicket.ATIVO);
        ticket.setValor(BigDecimal.ZERO);

        TicketEstacionamento salvo = ticketRepository.save(ticket);
        return new DadosDetalhamentoTicket(salvo);
    }

    @Transactional
    public DadosDetalhamentoTicket fechar(Long idTicket) {

        TicketEstacionamento ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ticket não encontrado!"));

        if (ticket.getStatus() != StatusTicket.ATIVO) {
            throw new ValidacaoException("Só é possível fechar tickets ATIVOS!");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        ticket.setFimTicket(agora);

        Duration duracao = Duration.between(ticket.getInicioTicket(), ticket.getFimTicket());
        long minutos = duracao.toMinutes();
        if (minutos <= 0) minutos = 1;

        long horas = (minutos + 59) / 60;

        BigDecimal tarifa = ticket.getZona().getTarifa();
        BigDecimal valorCobrado = tarifa.multiply(BigDecimal.valueOf(horas));

        ticket.setValor(valorCobrado);
        ticket.setAtivo(false);
        ticket.setStatus(StatusTicket.FECHADO);

        return new DadosDetalhamentoTicket(ticket);
    }
    
    @Transactional
    public DadosDetalhamentoTicket renovar(Long idTicket) {
        return renovar(idTicket, null);
    }

    @Transactional
    public DadosDetalhamentoTicket renovar(Long idTicket, DadosRenovacaoTicket dados) {
        TicketEstacionamento ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ticket não encontrado!"));

        if (ticket.getStatus() != StatusTicket.ATIVO) {
            throw new ValidacaoException("Só é possível renovar tickets ATIVOS!");
        }

        LocalDateTime venceEm = ticket.getVenceEm();
        if (venceEm == null) {
            throw new ValidacaoException("Ticket ativo sem horário de vencimento!");
        }

        if (!LocalDateTime.now(clock).isBefore(venceEm)) {
            throw new ValidacaoException("Ticket vencido não pode ser renovado!");
        }

        long minutos = (dados != null && dados.minutosAdicionais() != null)
                ? dados.minutosAdicionais()
                : ticket.getZona().getTempoMaximo();

        ticket.setVenceEm(venceEm.plusMinutes(minutos));
        return new DadosDetalhamentoTicket(ticket);
    }

    @Transactional
    public DadosDetalhamentoTicket cancelar(Long idTicket) {
        TicketEstacionamento ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ticket não encontrado!"));

        if (ticket.getStatus() != StatusTicket.ATIVO && ticket.getStatus() != StatusTicket.RESERVADO) {
            throw new ValidacaoException("Só é possível cancelar tickets ATIVOS ou RESERVADOS!");
        }

        ticket.setFimTicket(LocalDateTime.now(clock));
        ticket.setValor(BigDecimal.ZERO);
        ticket.setAtivo(false);
        ticket.setStatus(StatusTicket.CANCELADO);

        return new DadosDetalhamentoTicket(ticket);
    }

    @Transactional(readOnly = true)
    public boolean podeVisualizar(Long idTicket) {

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TicketEstacionamento ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ticket não encontrado!"));

        if (usuario.getTipo() == TipoUsuario.ADMINISTRADOR || usuario.getTipo() == TipoUsuario.FISCAL) {
            return true;
        }

        return ticket.getVeiculo().getDono().getIdUsuario().equals(usuario.getIdUsuario());
    }

    @Transactional(readOnly = true)
    public boolean podeFechar(Long idTicket) {
        return podeVisualizar(idTicket);
    }

    @Transactional(readOnly = true)
    public boolean podeCancelar(Long idTicket) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (usuario.getTipo() == TipoUsuario.ADMINISTRADOR || usuario.getTipo() == TipoUsuario.FISCAL) {
            return true;
        }

        TicketEstacionamento ticket = ticketRepository.findById(idTicket).orElseThrow(() -> new RecursoNaoEncontradoException("Ticket não encontrado!"));

        boolean dono = ticket.getVeiculo().getDono().getIdUsuario().equals(usuario.getIdUsuario());
        if (!dono) return false;

        return ticket.getStatus() == StatusTicket.RESERVADO || ticket.getStatus() == StatusTicket.ATIVO;
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoTicket detalhar(Long id) {
        TicketEstacionamento ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ticket não encontrado!"));
        return new DadosDetalhamentoTicket(ticket);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoTicket> listarTodos(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(DadosDetalhamentoTicket::new);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoTicket> listarMeus(Pageable pageable) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ticketRepository.findByVeiculo_Dono_IdUsuario(usuario.getIdUsuario(), pageable)
                .map(DadosDetalhamentoTicket::new);
    }

    private void validarStatusVeiculo(Veiculo veiculo) {
        if (veiculo.getStatus() != null && veiculo.getStatus() != StatusVeiculo.ATIVO) {
            throw new ValidacaoException("Veículo inativo!");
        }
    }

    private void validarStatusZona(ZonaEstacionamento zona) {
        if (zona.getStatus() != null && zona.getStatus() != StatusZona.ATIVA) {
            throw new ValidacaoException("Zona inativa!");
        }
    }

    private void validarHorarioZona(ZonaEstacionamento zona, LocalTime agora) {
        LocalTime inicio = zona.getHoraInicio();
        LocalTime fim = zona.getHoraFim();

        if (inicio == null || fim == null) return;

        boolean dentro;
        if (!inicio.isAfter(fim)) {
            dentro = !agora.isBefore(inicio) && !agora.isAfter(fim);
        } else {
            dentro = !agora.isBefore(inicio) || !agora.isAfter(fim);
        }

        if (!dentro) {
            throw new ValidacaoException("Fora do horário permitido da zona!");
        }
    }

    @Transactional
    public DadosDetalhamentoTicket reservar(DadosReservaTicket dados) {

        Veiculo veiculo = veiculoRepository.findById(dados.idVeiculo())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado!"));

        ZonaEstacionamento zona = zonaRepository.findById(dados.idZonaEstacionamento())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Zona não encontrada!"));

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        validarStatusVeiculo(veiculo);
        validarFechadoSemPagamento(veiculo);
        validarStatusZona(zona);
        validarHorarioZona(zona, LocalTime.now(clock));

        if (usuario.getTipo() == TipoUsuario.MOTORISTA || usuario.getTipo() == TipoUsuario.EMPRESA) {
            if (!veiculo.getDono().getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new AccessDeniedException("Você não pode reservar vaga para veículo de outro usuário!");
            }
        }

        boolean existeAtivo = ticketRepository.existsByVeiculo_IdAndStatus(veiculo.getId(), StatusTicket.ATIVO);
        if (existeAtivo) {
            throw new ValidacaoException("Já existe um ticket ativo para este veículo!");
        }
        boolean existeReservado = ticketRepository.existsByVeiculo_IdAndStatus(veiculo.getId(), StatusTicket.RESERVADO);
        if (existeReservado) {
            throw new ValidacaoException("Já existe uma reserva para este veículo!");
        }

        int capacidade = (zona.getCapacidadeVagas() != null) ? zona.getCapacidadeVagas() : 0;

        long ocupadas = ticketRepository.countByZona_IdZonaEstacionamentoAndStatusIn(
                zona.getIdZonaEstacionamento(),
                List.of(StatusTicket.ATIVO, StatusTicket.RESERVADO)
        );

        if (capacidade <= 0 || ocupadas >= capacidade) {
            throw new ValidacaoException("Zona lotada!");
        }

        TicketEstacionamento ticket = new TicketEstacionamento();
        ticket.setVeiculo(veiculo);
        ticket.setZona(zona);
        ticket.setInicioTicket(LocalDateTime.now(clock));
        ticket.setVenceEm(null);
        ticket.setFimTicket(null);
        ticket.setAtivo(true);
        ticket.setStatus(StatusTicket.RESERVADO);
        ticket.setValor(BigDecimal.ZERO);

        TicketEstacionamento salvo = ticketRepository.save(ticket);
        return new DadosDetalhamentoTicket(salvo);
    }

    @Transactional
    public DadosDetalhamentoTicket iniciar(Long idTicket) {
        var ticket = ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ticket não encontrado!"));

        if (ticket.getStatus() != StatusTicket.RESERVADO) {
            throw new ValidacaoException("Só é possível iniciar tickets RESERVADOS!");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime limite = ticket.getInicioTicket().plusMinutes(10);
        LocalDateTime inicioTicket = agora.isAfter(limite) ? limite : agora;
        ticket.setStatus(StatusTicket.ATIVO);
        ticket.setAtivo(true);
        ticket.setInicioTicket(inicioTicket); // zera o “tempo grátis”
        ticket.setVenceEm(inicioTicket.plusMinutes(ticket.getZona().getTempoMaximo()));
        ticket.setFimTicket(null);
        ticket.setValor(BigDecimal.ZERO);

        return new DadosDetalhamentoTicket(ticket);
    }

    @Transactional
    public int iniciarReservasAtrasadas() {
        LocalDateTime limite = LocalDateTime.now(clock).minusMinutes(10);

        List<TicketEstacionamento> reservas = ticketRepository
                .findByStatusAndAtivoTrueAndInicioTicketLessThanEqual(StatusTicket.RESERVADO, limite);

        for (TicketEstacionamento t : reservas) {

            if (t.getZona() != null && t.getZona().getStatus() != StatusZona.ATIVA) {
                t.setStatus(StatusTicket.CANCELADO);
                t.setAtivo(false);
                t.setFimTicket(LocalDateTime.now(clock));
                t.setValor(BigDecimal.ZERO);
                continue;
            }

            t.setStatus(StatusTicket.ATIVO);
            LocalDateTime inicioTicket = t.getInicioTicket().plusMinutes(10);
            t.setInicioTicket(inicioTicket);
            t.setVenceEm(inicioTicket.plusMinutes(t.getZona().getTempoMaximo()));
            t.setFimTicket(null);
            t.setValor(BigDecimal.ZERO);
            t.setAtivo(true);
        }

        return reservas.size();
    }

    @Transactional
    public int expirarReservasAtrasadas() {
        LocalDateTime limite = LocalDateTime.now(clock).minusMinutes(10);

        List<TicketEstacionamento> reservas = ticketRepository
                .findByStatusAndAtivoTrueAndInicioTicketLessThanEqual(StatusTicket.RESERVADO, limite);

        for (TicketEstacionamento t : reservas) {
            t.setStatus(StatusTicket.CANCELADO);
            t.setAtivo(false);
            t.setFimTicket(LocalDateTime.now(clock));
            t.setValor(BigDecimal.ZERO);
        }

        return reservas.size();
    }

    private void validarFechadoSemPagamento(Veiculo veiculo) {

        var ultimoFechadoOpt =
                ticketRepository.findTop1ByVeiculo_IdAndStatusOrderByFimTicketDesc(
                        veiculo.getId(),
                        StatusTicket.FECHADO);

        if (ultimoFechadoOpt.isEmpty()) return;

        var ultimoFechado = ultimoFechadoOpt.get();

        boolean pagoAprovado = pagamentoRepository.existsByTicket_IdTicketAndStatusIn(
                ultimoFechado.getIdTicket(),
                List.of(StatusPagamento.APROVADO)
        );

        if (!pagoAprovado) {
            throw new ValidacaoException("Existe ticket fechado sem pagamento. Efetue o pagamento antes de abrir um novo");
        }
    }
}
