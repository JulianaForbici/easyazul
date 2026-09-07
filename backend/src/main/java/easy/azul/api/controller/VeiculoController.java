package easy.azul.api.controller;

import easy.azul.api.dto.Veiculo.DadosCadastroVeiculo;
import easy.azul.api.dto.Veiculo.DadosAtualizacaoVeiculo;
import easy.azul.api.dto.Veiculo.DadosDetalhamentoVeiculo;
import easy.azul.api.service.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("veiculos")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @PostMapping
    public ResponseEntity<DadosDetalhamentoVeiculo> cadastrar(@RequestBody @Valid DadosCadastroVeiculo dados, UriComponentsBuilder uriBuilder) {
        var detalhe = veiculoService.cadastrar(dados);
        var uri = uriBuilder.path("/veiculos/{id}").buildAndExpand(1L).toUri(); // ID mockado para buildar
        return ResponseEntity.created(uri).body(detalhe);
    }

    @GetMapping
    public ResponseEntity<Page<DadosDetalhamentoVeiculo>> listar(@PageableDefault(size = 10, sort = {"placa"}) Pageable paginacao) {
        var pagina = veiculoService.listarDoUsuarioLogado(paginacao);
        return ResponseEntity.ok(pagina);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoVeiculo> atualizar(@PathVariable Long id, @RequestBody @Valid DadosAtualizacaoVeiculo dados) {
        var detalhe = veiculoService.atualizar(id, dados);
        return ResponseEntity.ok(detalhe);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoVeiculo> detalhar(@PathVariable Long id) {
        var optionalVeiculo = veiculoService.buscarPorId(id);
        if (optionalVeiculo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var detalhe = new DadosDetalhamentoVeiculo(optionalVeiculo.get());
        return ResponseEntity.ok(detalhe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        veiculoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
