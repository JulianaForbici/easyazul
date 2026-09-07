package easy.azul.api.controller;

import easy.azul.api.dto.Ticket.DadosCadastroTicket;
import easy.azul.api.dto.Ticket.DadosDetalhamentoTicket;
import easy.azul.api.dto.Ticket.DadosReservaTicket;
import easy.azul.api.dto.Ticket.DadosRenovacaoTicket;
import easy.azul.api.service.TicketEstacionamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("tickets")
public class TicketEstacionamentoController {

    @Autowired
    private TicketEstacionamentoService ticketService;

    @PostMapping("/abrir")
    public ResponseEntity<DadosDetalhamentoTicket> abrir(@RequestBody @Valid DadosCadastroTicket dados, UriComponentsBuilder uriBuilder) {
        var detalhe = ticketService.abrir(dados);
        var uri = uriBuilder.path("/tickets/{id}").buildAndExpand(1L).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @PutMapping("/{id}/fechar")
    public ResponseEntity<DadosDetalhamentoTicket> fechar(@PathVariable Long id) {
        var detalhe = ticketService.fechar(id);
        return ResponseEntity.ok(detalhe);
    }

    @PostMapping("/reservar")
    public ResponseEntity<DadosDetalhamentoTicket> reservar(@RequestBody @Valid DadosReservaTicket dados, UriComponentsBuilder uriBuilder) {
        var detalhe = ticketService.reservar(dados);
        var uri = uriBuilder.path("/tickets/{id}").buildAndExpand(1L).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @PutMapping("/{id}/renovar")
    public ResponseEntity<DadosDetalhamentoTicket> renovar(@PathVariable Long id, @RequestBody @Valid DadosRenovacaoTicket dados) {
        var detalhe = ticketService.renovar(id, dados);
        return ResponseEntity.ok(detalhe);
    }

    @GetMapping("/meus")
    public ResponseEntity<Page<DadosDetalhamentoTicket>> listarMeus(@PageableDefault(size = 10) Pageable paginacao) {
        var pagina = ticketService.listarMeus(paginacao);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping
    public ResponseEntity<Page<DadosDetalhamentoTicket>> listarTodos(@PageableDefault(size = 10) Pageable paginacao) {
        var pagina = ticketService.listarTodos(paginacao);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoTicket> detalhar(@PathVariable Long id) {
        var detalhe = ticketService.detalhar(id);
        return ResponseEntity.ok(detalhe);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        ticketService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
