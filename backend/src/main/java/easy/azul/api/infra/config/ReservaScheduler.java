package easy.azul.api.infra.config;

import easy.azul.api.service.TicketEstacionamentoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaScheduler {

    private final TicketEstacionamentoService ticketService;

    public ReservaScheduler(TicketEstacionamentoService ticketService) {
        this.ticketService = ticketService;
    }

    @Scheduled(fixedDelay = 60000) // a cada 60s
    public void promoverReservas() {
        ticketService.iniciarReservasAtrasadas();
    }
}