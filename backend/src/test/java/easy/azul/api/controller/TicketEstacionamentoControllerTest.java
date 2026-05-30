package easy.azul.api.controller;

import easy.azul.api.dto.Ticket.DadosCadastroTicket;
import easy.azul.api.dto.Ticket.DadosDetalhamentoTicket;
import easy.azul.api.service.TicketEstacionamentoService;
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
class TicketEstacionamentoControllerTest {

    @Mock
    private TicketEstacionamentoService ticketService;

    @InjectMocks
    private TicketEstacionamentoController controller;

    @Test
    void abrirDeveRetornarOkEBodyDoService() {
        var dados = mock(DadosCadastroTicket.class);
        var esperado = mock(DadosDetalhamentoTicket.class);

        when(ticketService.abrir(dados)).thenReturn(esperado);

        ResponseEntity<DadosDetalhamentoTicket> response = controller.abrir(dados);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(esperado, response.getBody());
        verify(ticketService).abrir(dados);
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void fecharDeveRetornarOkEBodyDoService() {
        Long id = 10L;
        var esperado = mock(DadosDetalhamentoTicket.class);

        when(ticketService.fechar(id)).thenReturn(esperado);

        ResponseEntity<DadosDetalhamentoTicket> response = controller.fechar(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(esperado, response.getBody());
        verify(ticketService).fechar(id);
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void cancelarDeveRetornarOkEBodyDoService() {
        Long id = 7L;
        var esperado = mock(DadosDetalhamentoTicket.class);

        when(ticketService.cancelar(id)).thenReturn(esperado);

        ResponseEntity<DadosDetalhamentoTicket> response = controller.cancelar(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(esperado, response.getBody());
        verify(ticketService).cancelar(id);
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void listarTodosDeveRepassarPageableParaService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DadosDetalhamentoTicket> page =
                new PageImpl<>(List.of(mock(DadosDetalhamentoTicket.class)), pageable, 1);

        when(ticketService.listarTodos(pageable)).thenReturn(page);

        Page<DadosDetalhamentoTicket> result = controller.listarTodos(pageable);

        assertSame(page, result);
        verify(ticketService).listarTodos(pageable);
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void listarMeusDeveRepassarPageableParaService() {
        Pageable pageable = PageRequest.of(1, 10);
        Page<DadosDetalhamentoTicket> page =
                new PageImpl<>(List.of(mock(DadosDetalhamentoTicket.class)), pageable, 1);

        when(ticketService.listarMeus(pageable)).thenReturn(page);

        Page<DadosDetalhamentoTicket> result = controller.listarMeus(pageable);

        assertSame(page, result);
        verify(ticketService).listarMeus(pageable);
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void detalharDeveRetornarOkEBodyDoService() {
        Long id = 55L;
        var esperado = mock(DadosDetalhamentoTicket.class);

        when(ticketService.detalhar(id)).thenReturn(esperado);

        ResponseEntity<DadosDetalhamentoTicket> response = controller.detalhar(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(esperado, response.getBody());
        verify(ticketService).detalhar(id);
        verifyNoMoreInteractions(ticketService);
    }
}