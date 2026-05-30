package easy.azul.api.controller;

import easy.azul.api.dto.Ticket.DadosCadastroTicket;
import easy.azul.api.dto.Ticket.DadosDetalhamentoTicket;
import easy.azul.api.dto.Ticket.DadosReservaTicket;
import easy.azul.api.service.TicketEstacionamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
public class TicketEstacionamentoController {

    @Autowired
    private TicketEstacionamentoService ticketService;

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('MOTORISTA','EMPRESA')")
    public ResponseEntity<DadosDetalhamentoTicket> abrir(@RequestBody @Valid DadosCadastroTicket dados) {
        return ResponseEntity.ok(ticketService.abrir(dados));
    }

    @PostMapping("/{id}/fechar")
    @Transactional
    @PreAuthorize("@ticketEstacionamentoService.podeFechar(#id)")
    public ResponseEntity<DadosDetalhamentoTicket> fechar(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.fechar(id));
    }

    @PostMapping("/{id}/cancelar")
    @Transactional
    @PreAuthorize("@ticketEstacionamentoService.podeCancelar(#id)")
    public ResponseEntity<DadosDetalhamentoTicket> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.cancelar(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISCAL')")
    public Page<DadosDetalhamentoTicket> listarTodos(Pageable pageable) {
        return ticketService.listarTodos(pageable);
    }

    @GetMapping("/meus")
    @PreAuthorize("hasAnyRole('MOTORISTA','EMPRESA')")
    public Page<DadosDetalhamentoTicket> listarMeus(Pageable pageable) {
        return ticketService.listarMeus(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ticketEstacionamentoService.podeVisualizar(#id)")
    public ResponseEntity<DadosDetalhamentoTicket> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.detalhar(id));
    }

    @PostMapping("/reservar")
    @Transactional
    @PreAuthorize("hasAnyRole('MOTORISTA','EMPRESA','ADMINISTRADOR','FISCAL')")
    public ResponseEntity<DadosDetalhamentoTicket> reservar(@RequestBody @Valid DadosReservaTicket dados) {
        return ResponseEntity.ok(ticketService.reservar(dados));
    }

    @PostMapping("/{id}/iniciar")
    @Transactional
    @PreAuthorize("@ticketEstacionamentoService.podeIniciar(#id)")
    public ResponseEntity<DadosDetalhamentoTicket> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.iniciar(id));
    }
}