package easy.azul.api.service;

import easy.azul.api.dto.Zona.DadosPinZona;
import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.Enum.StatusZona;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.ZonaEstacionamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZonaMapaService {

    @Autowired
    private ZonaEstacionamentoRepository zonaRepository;

    @Autowired
    private TicketEstacionamentoRepository ticketRepository;

    public List<DadosPinZona> listarPins() {

        var zonas = zonaRepository.findAll()
                .stream()
                .filter(z -> z.getStatus() == StatusZona.ATIVA)
                .toList();

        return zonas.stream().map(z -> {
            long ocupadas = ticketRepository.countByZona_IdZonaEstacionamentoAndStatus(
                    z.getIdZonaEstacionamento(),
                    StatusTicket.ATIVO);

            long disponiveis = Math.max(0, (long) z.getCapacidadeVagas() - ocupadas);

            return new DadosPinZona(
                    z.getIdZonaEstacionamento(),
                    z.getNome(),
                    z.getLatitude(),
                    z.getLongitude(),
                    z.getCapacidadeVagas(),
                    ocupadas,
                    disponiveis);}).toList();
    }

    public List<DadosPinZona> listarPinsProximas(Double lat, Double lon, Double raioMetros) {
        var pins = listarPins();

        return pins.stream()
                .filter(p -> distanciaMetros(lat, lon, p.latitude(), p.longitude()) <= raioMetros).toList();
    }

    private double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // raio da terra em metros

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}