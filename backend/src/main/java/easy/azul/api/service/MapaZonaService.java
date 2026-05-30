package easy.azul.api.service;

import easy.azul.api.dto.Zona.DadosMapaZona;
import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.Enum.StatusZona;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.ZonaEstacionamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import easy.azul.api.entity.Enum.DisponibilidadeZona;
import java.util.List;

@Service
public class MapaZonaService {

    private final ZonaEstacionamentoRepository zonaRepo;
    private final TicketEstacionamentoRepository ticketRepo;

    public MapaZonaService(ZonaEstacionamentoRepository zonaRepo,
                           TicketEstacionamentoRepository ticketRepo) {
        this.zonaRepo = zonaRepo;
        this.ticketRepo = ticketRepo;
    }

    @Transactional(readOnly = true)
    public List<DadosMapaZona> listarZonasMapa() {

        return zonaRepo.findByStatus(StatusZona.ATIVA)
                .stream()
                .map(z -> {

                    int capacidade = (z.getCapacidadeVagas() != null) ? z.getCapacidadeVagas() : 0;

                    long ocupadasLong = ticketRepo.countByZona_IdZonaEstacionamentoAndStatusIn(
                            z.getIdZonaEstacionamento(),
                            List.of(StatusTicket.ATIVO, StatusTicket.RESERVADO)
                    );

                    int ocupadas = (int) Math.min(Integer.MAX_VALUE, ocupadasLong);
                    int disponiveis = Math.max(0, capacidade - ocupadas);

                    var disp = calcularDisponibilidade(capacidade, ocupadas, disponiveis);

                    return new DadosMapaZona(
                            z.getIdZonaEstacionamento(),
                            z.getNome(),
                            z.getLatitude(),
                            z.getLongitude(),
                            capacidade,
                            ocupadas,
                            disponiveis,
                            disp
                    );
                })
                .toList();
    }

    private DisponibilidadeZona calcularDisponibilidade(int capacidade, int ocupadas, int disponiveis) {
        if (capacidade <= 0) {
            return DisponibilidadeZona.OCUPADA;
        }

        if (disponiveis == 0) {
            return DisponibilidadeZona.OCUPADA;
        }

        if (ocupadas > 0) {
            return DisponibilidadeZona.PARCIAL;
        }

        return DisponibilidadeZona.DISPONIVEL;
    }

}