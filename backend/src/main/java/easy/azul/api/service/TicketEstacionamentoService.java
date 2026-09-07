package easy.azul.api.service;

import easy.azul.api.dto.Ticket.DadosDetalhamentoTicket;
import easy.azul.api.dto.Ticket.DadosReservaTicket;
import easy.azul.api.entity.TicketEstacionamento;
import easy.azul.api.entity.Veiculo;
import easy.azul.api.entity.ZonaEstacionamento;
import easy.azul.api.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class TicketEstacionamentoService {

    public DadosDetalhamentoTicket reservar(DadosReservaTicket dados) {
        return new DadosDetalhamentoTicket(new TicketEstacionamento());
    }

    public Page<DadosDetalhamentoTicket> listar(Pageable paginacao) {
        return Page.empty();
    }

    public Page<DadosDetalhamentoTicket> listarMeus(Pageable paginacao) {
        return Page.empty();
    }

    public DadosDetalhamentoTicket buscarPorId(Long id) {
        return new DadosDetalhamentoTicket(new TicketEstacionamento());
    }

    public DadosDetalhamentoTicket detalhar(Long id) {
        return new DadosDetalhamentoTicket(new TicketEstacionamento());
    }

    public DadosDetalhamentoTicket iniciar(Long id) {
        return new DadosDetalhamentoTicket(new TicketEstacionamento());
    }

    public void cancelar(Long id) {
        // Lógica de cancelamento
    }

    public void iniciarReservasAtrasadas() {
        // Método exigido pelo Scheduler
    }
        public DadosDetalhamentoTicket renovar(java.lang.Long id, easy.azul.api.dto.Ticket.DadosRenovacaoTicket dados) {
        return new DadosDetalhamentoTicket(new TicketEstacionamento());
    }

    public DadosDetalhamentoTicket renovar(java.lang.Long id) {
        return new DadosDetalhamentoTicket(new TicketEstacionamento());
    }

    public Page<DadosDetalhamentoTicket> listarTodos(Pageable paginacao) {
        return Page.empty();
    }
    public DadosDetalhamentoTicket abrir(easy.azul.api.dto.Ticket.DadosCadastroTicket dados) {
        return new DadosDetalhamentoTicket(new TicketEstacionamento());
    }

    public DadosDetalhamentoTicket fechar(java.lang.Long id) {
        return new DadosDetalhamentoTicket(new TicketEstacionamento());
    }

}
