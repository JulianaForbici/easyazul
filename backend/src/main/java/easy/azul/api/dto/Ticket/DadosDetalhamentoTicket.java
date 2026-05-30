package easy.azul.api.dto.Ticket;
import easy.azul.api.entity.TicketEstacionamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DadosDetalhamentoTicket(
        Long id,
        Long idVeiculo,
        String placa,
        Long idDono,
        String nomeDono,
        Long idZona,
        String nomeZona,
        LocalDateTime inicioTicket,
        LocalDateTime fimTicket,
        BigDecimal valor,
        Boolean ativo,
        String status
) {
    public DadosDetalhamentoTicket(TicketEstacionamento ticket) {
        this(
                ticket.getIdTicket(),
                ticket.getVeiculo().getId(),
                ticket.getVeiculo().getPlaca(),
                ticket.getVeiculo().getDono().getIdUsuario(),
                ticket.getVeiculo().getDono().getNome(),
                ticket.getZona().getIdZonaEstacionamento(),
                ticket.getZona().getNome(),
                ticket.getInicioTicket(),
                ticket.getFimTicket(),
                ticket.getValor(),
                ticket.getAtivo(),
                ticket.getStatus().name()
        );
    }
}