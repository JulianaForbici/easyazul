package easy.azul.api.controller;

import easy.azul.api.dto.Zona.DadosAtualizacaoZona;
import easy.azul.api.dto.Zona.DadosCadastroZona;
import easy.azul.api.dto.Zona.DadosDetalhamentoZona;
import easy.azul.api.dto.Zona.DadosPinZona;
import easy.azul.api.entity.Enum.StatusZona;
import easy.azul.api.entity.ZonaEstacionamento;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.repository.ZonaEstacionamentoRepository;
import easy.azul.api.service.ZonaEstacionamentoService;
import easy.azul.api.service.ZonaMapaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZonaEstacionamentoControllerTest {

    @Mock
    private ZonaEstacionamentoService zonaService;

    @Mock
    private ZonaEstacionamentoRepository zonaEstacionamentoRepository;

    @Mock
    private ZonaMapaService zonaMapaService;

    @InjectMocks
    private ZonaEstacionamentoController controller;

    @Test
    void cadastrarDeveRetornarOkComBodyDoService() {
        var dados = mock(DadosCadastroZona.class);
        var dto = mock(DadosDetalhamentoZona.class);

        when(zonaService.cadastrar(dados)).thenReturn(dto);

        ResponseEntity<DadosDetalhamentoZona> response = controller.cadastrar(dados);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(zonaService).cadastrar(dados);
        verifyNoMoreInteractions(zonaService);
    }

    @Test
    void listarDeveBuscarApenasAtivasENaoEstourarNoMap() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("nome"));

        ZonaEstacionamento z1 = mock(ZonaEstacionamento.class);
        ZonaEstacionamento z2 = mock(ZonaEstacionamento.class);

        Page<ZonaEstacionamento> pageZonas = new PageImpl<>(List.of(z1, z2), pageable, 2);

        when(zonaEstacionamentoRepository.findByStatus(StatusZona.ATIVA, pageable))
                .thenReturn(pageZonas);

        Page<DadosDetalhamentoZona> result = controller.listar(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        verify(zonaEstacionamentoRepository).findByStatus(StatusZona.ATIVA, pageable);
        verifyNoMoreInteractions(zonaEstacionamentoRepository);
    }

    @Test
    void detalharQuandoEncontradaEAtivaDeveRetornarOk() {
        Long id = 1L;

        ZonaEstacionamento zona = mock(ZonaEstacionamento.class);
        when(zona.getStatus()).thenReturn(StatusZona.ATIVA);

        when(zonaEstacionamentoRepository.findById(id)).thenReturn(Optional.of(zona));

        ResponseEntity<DadosDetalhamentoZona> response = controller.detalhar(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(zonaEstacionamentoRepository).findById(id);
        verifyNoMoreInteractions(zonaEstacionamentoRepository);
    }

    @Test
    void detalharQuandoNaoEncontradaDeveLancarValidacaoException() {
        Long id = 999L;

        when(zonaEstacionamentoRepository.findById(id)).thenReturn(Optional.empty());

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> controller.detalhar(id));
        assertEquals("Zona não encontrada", ex.getMessage());

        verify(zonaEstacionamentoRepository).findById(id);
        verifyNoMoreInteractions(zonaEstacionamentoRepository);
    }

    @Test
    void detalharQuandoZonaInativaDeveLancarValidacaoException() {
        Long id = 2L;

        ZonaEstacionamento zona = mock(ZonaEstacionamento.class);
        when(zona.getStatus()).thenReturn(StatusZona.INATIVA);

        when(zonaEstacionamentoRepository.findById(id)).thenReturn(Optional.of(zona));

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> controller.detalhar(id));
        assertEquals("Zona inativa.", ex.getMessage());

        verify(zonaEstacionamentoRepository).findById(id);
        verifyNoMoreInteractions(zonaEstacionamentoRepository);
    }

    @Test
    void atualizarDeveChamarServiceEDevolverOk() {
        Long id = 10L;
        var dados = mock(DadosAtualizacaoZona.class);
        var dto = mock(DadosDetalhamentoZona.class);

        when(zonaService.atualizar(id, dados)).thenReturn(dto);

        ResponseEntity<DadosDetalhamentoZona> response = controller.atualizar(id, dados);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(zonaService).atualizar(id, dados);
        verifyNoMoreInteractions(zonaService);
    }

    @Test
    void excluirDeveChamarServiceEDevolverNoContent() {
        Long id = 5L;

        ResponseEntity<Void> response = controller.excluir(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(zonaService).excluir(id);
        verifyNoMoreInteractions(zonaService);
    }

    @Test
    void listarTodasDeveRetornarPageMapeadaDoRepositorio() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("nome"));

        ZonaEstacionamento z1 = mock(ZonaEstacionamento.class);
        Page<ZonaEstacionamento> pageZonas = new PageImpl<>(List.of(z1), pageable, 1);

        when(zonaEstacionamentoRepository.findAll(pageable)).thenReturn(pageZonas);

        Page<DadosDetalhamentoZona> result = controller.listarTodas(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(zonaEstacionamentoRepository).findAll(pageable);
        verifyNoMoreInteractions(zonaEstacionamentoRepository);
    }

    @Test
    void pinsDeveRetornarOkComListaDoService() {
        List<DadosPinZona> pins = List.of(mock(DadosPinZona.class), mock(DadosPinZona.class));

        when(zonaMapaService.listarPins()).thenReturn(pins);

        ResponseEntity<List<DadosPinZona>> response = controller.pins();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(pins, response.getBody());

        verify(zonaMapaService).listarPins();
        verifyNoMoreInteractions(zonaMapaService);
    }

    @Test
    void proximasDeveRetornarOkComListaDoService() {
        double lat = -23.5;
        double lon = -46.6;
        double raio = 2000.0;

        List<DadosPinZona> pins = List.of(mock(DadosPinZona.class));

        when(zonaMapaService.listarPinsProximas(lat, lon, raio)).thenReturn(pins);

        ResponseEntity<List<DadosPinZona>> response = controller.proximas(lat, lon, raio);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(pins, response.getBody());

        verify(zonaMapaService).listarPinsProximas(lat, lon, raio);
        verifyNoMoreInteractions(zonaMapaService);
    }
}