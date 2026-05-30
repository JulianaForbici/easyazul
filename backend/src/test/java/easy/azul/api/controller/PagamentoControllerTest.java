package easy.azul.api.controller;

import easy.azul.api.dto.Pagamento.DadosCadastroPagamento;
import easy.azul.api.dto.Pagamento.DadosCancelamentoPagamento;
import easy.azul.api.dto.Pagamento.DadosDetalhamentoPagamento;
import easy.azul.api.service.PagamentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoControllerTest {

    @Mock
    private PagamentoService pagamentoService;

    @InjectMocks
    private PagamentoController controller;

    @Test
    void criarDeveRetornarOkEBodyDoService() {
        var dados = mock(DadosCadastroPagamento.class);
        var esperado = mock(DadosDetalhamentoPagamento.class);

        when(pagamentoService.criar(dados)).thenReturn(esperado);

        ResponseEntity<DadosDetalhamentoPagamento> response = controller.criar(dados);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(esperado, response.getBody());
        verify(pagamentoService).criar(dados);
        verifyNoMoreInteractions(pagamentoService);
    }

    @Test
    void confirmarDeveRetornarOkEBodyDoService() {
        Long id = 10L;
        var esperado = mock(DadosDetalhamentoPagamento.class);

        when(pagamentoService.confirmar(id)).thenReturn(esperado);

        ResponseEntity<DadosDetalhamentoPagamento> response = controller.confirmar(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(esperado, response.getBody());
        verify(pagamentoService).confirmar(id);
        verifyNoMoreInteractions(pagamentoService);
    }

    @Test
    void listarMeusDeveRetornarOkComPageDoService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DadosDetalhamentoPagamento> page = new PageImpl<>(List.of(mock(DadosDetalhamentoPagamento.class)), pageable, 1);

        when(pagamentoService.listarMeus(pageable)).thenReturn(page);

        ResponseEntity<Page<DadosDetalhamentoPagamento>> response = controller.listarMeus(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
        verify(pagamentoService).listarMeus(pageable);
        verifyNoMoreInteractions(pagamentoService);
    }

    @Test
    void cancelarDeveChamarServiceComMotivoEDevolverOk() {
        Long id = 5L;

        var dadosCancelamento = mock(DadosCancelamentoPagamento.class);
        when(dadosCancelamento.motivo()).thenReturn("Desisti");

        var esperado = mock(DadosDetalhamentoPagamento.class);
        when(pagamentoService.cancelar(id, "Desisti")).thenReturn(esperado);

        ResponseEntity<DadosDetalhamentoPagamento> response = controller.cancelar(id, dadosCancelamento);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(esperado, response.getBody());
        verify(dadosCancelamento).motivo();
        verify(pagamentoService).cancelar(id, "Desisti");
        verifyNoMoreInteractions(pagamentoService);
    }

    @Test
    void listarQuandoTicketIdNuloDeveRepassarNullParaService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DadosDetalhamentoPagamento> page = new PageImpl<>(List.of(), pageable, 0);

        when(pagamentoService.listar(null, pageable)).thenReturn(page);

        Page<DadosDetalhamentoPagamento> resultado = controller.listar(null, pageable);

        assertSame(page, resultado);
        verify(pagamentoService).listar(null, pageable);
        verifyNoMoreInteractions(pagamentoService);
    }

    @Test
    void listarQuandoTicketIdInformadoDeveRepassarIdParaService() {
        Long ticketId = 99L;
        Pageable pageable = PageRequest.of(1, 10);
        Page<DadosDetalhamentoPagamento> page = new PageImpl<>(List.of(mock(DadosDetalhamentoPagamento.class)), pageable, 1);

        when(pagamentoService.listar(ticketId, pageable)).thenReturn(page);

        Page<DadosDetalhamentoPagamento> resultado = controller.listar(ticketId, pageable);

        assertSame(page, resultado);
        verify(pagamentoService).listar(ticketId, pageable);
        verifyNoMoreInteractions(pagamentoService);
    }
}