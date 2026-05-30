package easy.azul.api.service;

import easy.azul.api.dto.APIMapas.NominatimResponse;
import easy.azul.api.dto.Zona.DadosAtualizacaoZona;
import easy.azul.api.dto.Zona.DadosCadastroZona;
import easy.azul.api.dto.Zona.DadosDetalhamentoZona;
import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.Enum.StatusZona;
import easy.azul.api.entity.ZonaEstacionamento;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.ZonaEstacionamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZonaEstacionamentoServiceTest {

    @Mock
    private ZonaEstacionamentoRepository zonaEstacionamentoRepository;

    @Mock
    private TicketEstacionamentoRepository ticketEstacionamentoRepository;

    @Mock
    private MapaService mapaService;

    @InjectMocks
    private ZonaEstacionamentoService service;

    @BeforeEach
    void setUp() {
    }


    private DadosCadastroZona dadosCadastro(
            String nome,
            String endereco,
            String descricao,
            Double latitude,
            Double longitude,
            BigDecimal tarifa,
            Integer tempoMaximo,
            LocalTime horaInicio,
            LocalTime horaFim,
            Integer capacidadeVagas
    ) {
        return new DadosCadastroZona(
                nome,
                endereco,
                descricao,
                latitude,
                longitude,
                tarifa,
                tempoMaximo,
                horaInicio,
                horaFim,
                capacidadeVagas
        );
    }

    private DadosAtualizacaoZona dadosAtualizacao(
            String nome,
            BigDecimal tarifa,
            String descricao,
            Integer tempoMaximo,
            LocalTime horaInicio,
            LocalTime horaFim,
            Double latitude,
            Double longitude,
            Integer capacidadeVagas,
            String endereco
    ) {
        return new DadosAtualizacaoZona(
                nome,
                tarifa,
                descricao,
                tempoMaximo,
                horaInicio,
                horaFim,
                latitude,
                longitude,
                null,
                null,
                null,
                capacidadeVagas,
                endereco);
    }

    private ZonaEstacionamento zonaBase(Long id) {
        ZonaEstacionamento z = new ZonaEstacionamento();
        z.setIdZonaEstacionamento(id);
        z.setNome("Zona Central");
        z.setTarifa(new BigDecimal("5.00"));
        z.setDescricao("desc");
        z.setTempoMaximo(2);
        z.setHoraInicio(LocalTime.of(8, 0));
        z.setHoraFim(LocalTime.of(18, 0));
        z.setLatitude(-23.55);
        z.setLongitude(-46.63);
        z.setCapacidadeVagas(100);
        z.setStatus(StatusZona.ATIVA);
        return z;
    }

    @Test
    void cadastrarQuandoNomeDuplicadoDeveLancarValidacao() {
        DadosCadastroZona dados = dadosCadastro(
                "  Zona   Central  ",
                null,
                "desc",
                -23.55,
                -46.63,
                new BigDecimal("5.00"),
                2,
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                10
        );

        when(zonaEstacionamentoRepository.existsByNomeIgnoreCase("Zona Central")).thenReturn(true);

        assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        verify(zonaEstacionamentoRepository).existsByNomeIgnoreCase("Zona Central");
        verify(zonaEstacionamentoRepository, never()).save(any());
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void cadastrarQuandoHoraInicioNaoEhAntesHoraFimDeveLancarValidacao() {
        DadosCadastroZona dados = dadosCadastro(
                "Zona A",
                null,
                "desc",
                -23.55,
                -46.63,
                new BigDecimal("5.00"),
                2,
                LocalTime.of(18, 0),
                LocalTime.of(8, 0),
                10);

        when(zonaEstacionamentoRepository.existsByNomeIgnoreCase("Zona A")).thenReturn(false);

        assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        verify(zonaEstacionamentoRepository).existsByNomeIgnoreCase("Zona A");
        verify(zonaEstacionamentoRepository, never()).save(any());
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void cadastrarQuandoNaoTemLatLonNemEnderecoDeveLancarValidacao() {
        DadosCadastroZona dados = dadosCadastro(
                "Zona A",
                "   ",
                "desc",
                null,
                null,
                new BigDecimal("5.00"),
                2,
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                10);

        when(zonaEstacionamentoRepository.existsByNomeIgnoreCase("Zona A")).thenReturn(false);

        assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        verify(zonaEstacionamentoRepository).existsByNomeIgnoreCase("Zona A");
        verify(zonaEstacionamentoRepository, never()).save(any());
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void cadastrarQuandoCapacidadeVagasNullDeveLancarValidacao() {
        DadosCadastroZona dados = dadosCadastro(
                "Zona A",
                null,
                "desc",
                -23.55,
                -46.63,
                new BigDecimal("5.00"),
                2,
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                null
        );

        when(zonaEstacionamentoRepository.existsByNomeIgnoreCase("Zona A")).thenReturn(false);

        assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        verify(zonaEstacionamentoRepository).existsByNomeIgnoreCase("Zona A");
        verify(zonaEstacionamentoRepository, never()).save(any());
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void cadastrarQuandoCapacidadeVagasNegativaDeveLancarValidacao() {
        DadosCadastroZona dados = dadosCadastro(
                "Zona A",
                null,
                "desc",
                -23.55,
                -46.63,
                new BigDecimal("5.00"),
                2,
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                -1);

        when(zonaEstacionamentoRepository.existsByNomeIgnoreCase("Zona A")).thenReturn(false);

        assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        verify(zonaEstacionamentoRepository).existsByNomeIgnoreCase("Zona A");
        verify(zonaEstacionamentoRepository, never()).save(any());
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void cadastrarQuandoLatLonFornecidoNaoDeveChamarMapaService() {
        DadosCadastroZona dados = dadosCadastro(
                "Zona A",
                "Rua X, 123",
                "desc",
                -23.55,
                -46.63,
                new BigDecimal("5.00"),
                2,
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                10
        );

        when(zonaEstacionamentoRepository.existsByNomeIgnoreCase("Zona A")).thenReturn(false);
        when(zonaEstacionamentoRepository.save(any(ZonaEstacionamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.cadastrar(dados);

        verify(mapaService, never()).buscarPorEndereco(anyString());
        verify(zonaEstacionamentoRepository).save(any(ZonaEstacionamento.class));
        verifyNoInteractions(ticketEstacionamentoRepository);
    }

    @Test
    void atualizarQuandoZonaNaoExisteDeveLancarValidacao() {
        when(zonaEstacionamentoRepository.findById(10L)).thenReturn(Optional.empty());

        DadosAtualizacaoZona dados = dadosAtualizacao(
                "Novo Nome",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(ValidacaoException.class, () -> service.atualizar(10L, dados));
        verify(zonaEstacionamentoRepository).findById(10L);
        verifyNoMoreInteractions(zonaEstacionamentoRepository);
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void atualizarQuandoZonaInativaDeveLancarValidacao() {
        ZonaEstacionamento zona = zonaBase(1L);
        zona.setStatus(StatusZona.INATIVA);
        when(zonaEstacionamentoRepository.findById(1L)).thenReturn(Optional.of(zona));

        DadosAtualizacaoZona dados = dadosAtualizacao(
                "Novo Nome",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(ValidacaoException.class, () -> service.atualizar(1L, dados));
        verify(zonaEstacionamentoRepository).findById(1L);
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void atualizarQuandoMudarNomeParaUmQueJaExisteDeveLancarValidacao() {
        ZonaEstacionamento zona = zonaBase(1L);
        when(zonaEstacionamentoRepository.findById(1L)).thenReturn(Optional.of(zona));
        when(zonaEstacionamentoRepository.existsByNomeIgnoreCase("Zona Nova")).thenReturn(true);

        DadosAtualizacaoZona dados = dadosAtualizacao(
                "  Zona   Nova  ",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(ValidacaoException.class, () -> service.atualizar(1L, dados));

        verify(zonaEstacionamentoRepository).findById(1L);
        verify(zonaEstacionamentoRepository).existsByNomeIgnoreCase("Zona Nova");
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void atualizarQuandoHoraFicarInvalidaDeveLancarValidacao() {
        ZonaEstacionamento zona = zonaBase(1L);
        when(zonaEstacionamentoRepository.findById(1L)).thenReturn(Optional.of(zona));

        DadosAtualizacaoZona dados = dadosAtualizacao(
                null,
                null,
                null,
                null,
                LocalTime.of(18, 0),
                LocalTime.of(8, 0),
                null,
                null,
                null,
                null);

        assertThrows(ValidacaoException.class, () -> service.atualizar(1L, dados));

        verify(zonaEstacionamentoRepository).findById(1L);
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void atualizarQuandoCapacidadeNegativaDeveLancarValidacao() {
        ZonaEstacionamento zona = zonaBase(1L);
        when(zonaEstacionamentoRepository.findById(1L)).thenReturn(Optional.of(zona));

        DadosAtualizacaoZona dados = dadosAtualizacao(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                -1,
                null);

        assertThrows(ValidacaoException.class, () -> service.atualizar(1L, dados));
        verify(zonaEstacionamentoRepository).findById(1L);
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void excluirQuandoZonaNaoExisteDeveLancarValidacao() {
        when(zonaEstacionamentoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ValidacaoException.class, () -> service.excluir(1L));
        verify(zonaEstacionamentoRepository).findById(1L);
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void excluirQuandoZonaJaInativaDeveLancarValidacao() {
        ZonaEstacionamento zona = zonaBase(1L);
        zona.setStatus(StatusZona.INATIVA);

        when(zonaEstacionamentoRepository.findById(1L)).thenReturn(Optional.of(zona));

        assertThrows(ValidacaoException.class, () -> service.excluir(1L));

        verify(zonaEstacionamentoRepository).findById(1L);
        verifyNoInteractions(ticketEstacionamentoRepository, mapaService);
    }

    @Test
    void excluirQuandoHaTicketAtivoNaZonaDeveLancarValidacao() {
        ZonaEstacionamento zona = zonaBase(1L);
        when(zonaEstacionamentoRepository.findById(1L)).thenReturn(Optional.of(zona));
        when(ticketEstacionamentoRepository.existsByZona_IdZonaEstacionamentoAndStatus(1L, StatusTicket.ATIVO))
                .thenReturn(true);

        assertThrows(ValidacaoException.class, () -> service.excluir(1L));

        verify(ticketEstacionamentoRepository)
                .existsByZona_IdZonaEstacionamentoAndStatus(1L, StatusTicket.ATIVO);
        assertEquals(StatusZona.ATIVA, zona.getStatus(), "não deve inativar se tem ticket ativo");
        verifyNoInteractions(mapaService);
    }

    @Test
    void excluirQuandoOkDeveMarcarComoInativa() {
        ZonaEstacionamento zona = zonaBase(1L);
        when(zonaEstacionamentoRepository.findById(1L)).thenReturn(Optional.of(zona));
        when(ticketEstacionamentoRepository.existsByZona_IdZonaEstacionamentoAndStatus(1L, StatusTicket.ATIVO))
                .thenReturn(false);

        service.excluir(1L);

        assertEquals(StatusZona.INATIVA, zona.getStatus());
        verify(ticketEstacionamentoRepository)
                .existsByZona_IdZonaEstacionamentoAndStatus(1L, StatusTicket.ATIVO);
        verifyNoInteractions(mapaService);
    }
}