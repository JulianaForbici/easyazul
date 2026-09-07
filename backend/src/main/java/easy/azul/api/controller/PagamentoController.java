package easy.azul.api.controller;

import easy.azul.api.dto.Pagamento.DadosCadastroPagamento;
import easy.azul.api.dto.Pagamento.DadosDetalhamentoPagamento;
import easy.azul.api.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService pagamentoService;

    @PostMapping
    public ResponseEntity<DadosDetalhamentoPagamento> registrar(@RequestBody @Valid DadosCadastroPagamento dados, UriComponentsBuilder uriBuilder) {
        var detalhe = pagamentoService.registrarPagamento(dados);
        var uri = uriBuilder.path("/pagamentos/{id}").buildAndExpand(1L).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmar(@PathVariable Long id) {
        pagamentoService.confirmarPagamento(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/meus")
    public ResponseEntity<Page<DadosDetalhamentoPagamento>> listarMeus(@PageableDefault(size = 10) Pageable paginacao) {
        var pagina = pagamentoService.listarMeus(paginacao);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<Page<DadosDetalhamentoPagamento>> listarPorUsuario(@PathVariable Long id, @PageableDefault(size = 10) Pageable paginacao) {
        var pagina = pagamentoService.listar(id, paginacao);
        return ResponseEntity.ok(pagina);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, @RequestBody String motivo) {
        pagamentoService.cancelar(id, motivo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoPagamento>> listarTodos() {
        var lista = pagamentoService.listarTodos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoPagamento> buscarPorId(@PathVariable Long id) {
        var detalhe = pagamentoService.buscarPorId(id);
        return ResponseEntity.ok(detalhe);
    }
}
