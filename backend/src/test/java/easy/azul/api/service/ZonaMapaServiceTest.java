package easy.azul.api.service;

import easy.azul.api.dto.Zona.DadosPinZona;
import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.Enum.StatusZona;
import easy.azul.api.entity.ZonaEstacionamento;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.ZonaEstacionamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZonaMapaServiceTest {

    @Mock ZonaEstacionamentoRepository zonaRepository;
    @Mock TicketEstacionamentoRepository ticketRepository;

    @InjectMocks ZonaMapaService zonaMapaService;

    @BeforeEach
    void beforeEach() {}

    private ZonaEstacionamento zonaAtiva(long id, String nome, double lat, double lon, int cap) {
        var z = mock(ZonaEstacionamento.class);
        when(z.getStatus()).thenReturn(StatusZona.ATIVA);
        when(z.getIdZonaEstacionamento()).thenReturn(id);
        when(z.getNome()).thenReturn(nome);
        when(z.getLatitude()).thenReturn(lat);
        when(z.getLongitude()).thenReturn(lon);
        when(z.getCapacidadeVagas()).thenReturn(cap);
        return z;
    }

    private ZonaEstacionamento zonaInativa() {
        var z = mock(ZonaEstacionamento.class);
        when(z.getStatus()).thenReturn(StatusZona.INATIVA);
        return z;
    }

    static Stream<Arguments> listarPinsCases() {
        return Stream.of(
                Arguments.of("filtra inativas",
                        List.of(
                                new ZonaCase(1L, "A", -26.90, -49.07, 10, StatusZona.ATIVA, 0),
                                new ZonaCase(2L, "B", -26.91, -49.08, 10, StatusZona.INATIVA, 0)
                        )
                ),
                Arguments.of("calcula vagas",
                        List.of(
                                new ZonaCase(3L, "Centro", -26.90, -49.07, 10, StatusZona.ATIVA, 3)
                        )
                ), Arguments.of("disp não negativa", List.of(new ZonaCase(4L, "Cheia", -26.90, -49.07, 2, StatusZona.ATIVA, 5))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listarPinsCases")
    @DisplayName("listarPins")
    void listarPinsDeveFuncionar(String caso, List<ZonaCase> zonas) {
        var mocks = zonas.stream().map(z -> {
            if (z.status == StatusZona.ATIVA) {
                return zonaAtiva(z.id, z.nome, z.lat, z.lon, z.cap);
            }
            return zonaInativa();
        }).toList();

        when(zonaRepository.findAll()).thenReturn(mocks);

        for (var z : zonas) {
            if (z.status == StatusZona.ATIVA) {
                when(ticketRepository.countByZona_IdZonaEstacionamentoAndStatus(z.id, StatusTicket.ATIVO))
                        .thenReturn(z.ocupadas);
            }
        }

        var pins = zonaMapaService.listarPins();

        if (caso.equals("filtra inativas")) {
            assertEquals(1, pins.size());
            assertEquals(1L, pins.get(0).idZona());
            verify(ticketRepository).countByZona_IdZonaEstacionamentoAndStatus(1L, StatusTicket.ATIVO);
        }

        if (caso.equals("calcula vagas")) {
            assertEquals(1, pins.size());
            var p = pins.get(0);
            assertEquals(10, p.capacidade());
            assertEquals(3L, p.ocupadas());
            assertEquals(7L, p.disponiveis());
        }

        if (caso.equals("disp não negativa")) {
            assertEquals(1, pins.size());
            var p = pins.get(0);
            assertEquals(2, p.capacidade());
            assertEquals(5L, p.ocupadas());
            assertEquals(0L, p.disponiveis());
        }
    }

    static Stream<Arguments> listarPinsProximasCases() {
        return Stream.of(
                Arguments.of("filtra por raio", -26.9000, -49.0700, 2000.0, List.of(
                                new ZonaCase(10L, "Perto", -26.9000, -49.0700, 10, StatusZona.ATIVA, 0),
                                new ZonaCase(11L, "Longe", -26.9500, -49.1200, 10, StatusZona.ATIVA, 0)),
                        List.of(10L)),
                Arguments.of("inclui no limite", -26.9000, -49.0700, 0.0, List.of(
                                new ZonaCase(20L, "Aqui", -26.9000, -49.0700, 10, StatusZona.ATIVA, 0)),
                        List.of(20L))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listarPinsProximasCases")
    @DisplayName("listarPinsProximas")
    void listarPinsProximasDeveFiltrar(String caso, double lat, double lon, double raio,
                                       List<ZonaCase> zonas, List<Long> idsEsperados) {

        var mocks = zonas.stream()
                .map(z -> zonaAtiva(z.id, z.nome, z.lat, z.lon, z.cap))
                .toList();

        when(zonaRepository.findAll()).thenReturn(mocks);

        for (var z : zonas) {
            when(ticketRepository.countByZona_IdZonaEstacionamentoAndStatus(z.id, StatusTicket.ATIVO))
                    .thenReturn(z.ocupadas);
        }

        var pins = zonaMapaService.listarPinsProximas(lat, lon, raio);

        assertEquals(idsEsperados, pins.stream().map(DadosPinZona::idZona).toList());
    }

    private record ZonaCase(
            long id,
            String nome,
            double lat,
            double lon,
            int cap,
            StatusZona status,
            long ocupadas
    ) {}
}