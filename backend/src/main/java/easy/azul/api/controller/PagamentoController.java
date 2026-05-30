package easy.azul.api.controller;

import easy.azul.api.dto.Pagamento.DadosCadastroPagamento;
import easy.azul.api.dto.Pagamento.DadosCancelamentoPagamento;
import easy.azul.api.dto.Pagamento.DadosDetalhamentoPagamento;
import easy.azul.api.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService pagamentoService;

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('MOTORISTA','EMPRESA')")
    public ResponseEntity<DadosDetalhamentoPagamento> criar(
            @RequestBody @Valid DadosCadastroPagamento dados
    ) {
        return ResponseEntity.ok(pagamentoService.criar(dados));
    }

    @PostMapping("/{id}/confirmar")
    @Transactional
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISCAL')")
    public ResponseEntity<DadosDetalhamentoPagamento> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.confirmar(id));
    }

    @GetMapping("/meus")
    @PreAuthorize("hasAnyRole('MOTORISTA','EMPRESA')")
    public ResponseEntity<Page<DadosDetalhamentoPagamento>> listarMeus(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(pagamentoService.listarMeus(pageable));
    }

    @PostMapping("/{id}/cancelar")
    @Transactional
    @PreAuthorize("@pagamentoService.podeCancelar(#id)")
    public ResponseEntity<DadosDetalhamentoPagamento> cancelar(
            @PathVariable Long id,
            @RequestBody @Valid DadosCancelamentoPagamento dados
    ) {
        return ResponseEntity.ok(pagamentoService.cancelar(id, dados.motivo()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISCAL')")
    public Page<DadosDetalhamentoPagamento> listar(
            @RequestParam(required = false) Long ticketId,
            @PageableDefault(size = 10) Pageable paginacao
    ) {
        return pagamentoService.listar(ticketId, paginacao);
    }
}