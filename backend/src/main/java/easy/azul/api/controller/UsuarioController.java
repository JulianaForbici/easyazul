package easy.azul.api.controller;

import easy.azul.api.dto.Usuario.DadosCadastroUsuario;
import easy.azul.api.dto.Usuario.DadosAtualizacaoUsuario;
import easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario;
import easy.azul.api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<DadosDetalhamentoUsuario> cadastrar(@RequestBody @Valid DadosCadastroUsuario dados, UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoUsuario detalhe = usuarioService.cadastrar(dados);
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(1L).toUri(); // ID fictício para compilar
        return ResponseEntity.created(uri).body(detalhe);
    }

    @PostMapping("/admin")
    public ResponseEntity<DadosDetalhamentoUsuario> cadastrarAdmin(@RequestBody @Valid DadosCadastroUsuario dados, UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoUsuario detalhe = usuarioService.cadastrarAdmin(dados);
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(1L).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listar() {
        var lista = usuarioService.listarTodos().stream().map(DadosDetalhamentoUsuario::new).toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoUsuario> detalhar(@PathVariable Long id) {
        DadosDetalhamentoUsuario detalhe = usuarioService.detalhar(id);
        return ResponseEntity.ok(detalhe);
    }

    @PutMapping
    public ResponseEntity<DadosDetalhamentoUsuario> atualizar(@RequestBody @Valid DadosAtualizacaoUsuario dados) {
        DadosDetalhamentoUsuario detalhe = usuarioService.atualizar(dados);
        return ResponseEntity.ok(detalhe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        usuarioService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        usuarioService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
