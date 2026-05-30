//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package easy.azul.api.controller;

import easy.azul.api.dto.Usuario.DadosAtualizacaoUsuario;
import easy.azul.api.dto.Usuario.DadosCadastroUsuario;
import easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario;
import easy.azul.api.entity.Enum.StatusUsuario;
import easy.azul.api.repository.UsuarioRepository;
import easy.azul.api.service.UsuarioService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping({"/usuarios"})
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<DadosDetalhamentoUsuario> cadastrar(@RequestBody @Valid DadosCadastroUsuario dados, UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoUsuario dto = this.usuarioService.cadastrar(dados);
        URI uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(new Object[]{dto.id()}).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PostMapping({"/admin"})
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DadosDetalhamentoUsuario> cadastrarAdmin(@RequestBody @Valid DadosCadastroUsuario dados, UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoUsuario dto = this.usuarioService.cadastrarAdmin(dados);
        URI uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(new Object[]{dto.id()}).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<DadosDetalhamentoUsuario> listar(@RequestParam(required = false) StatusUsuario status, @PageableDefault(size = 10,sort = {"nome"}) Pageable paginacao) {
        return status == null ? this.usuarioRepository.findAll(paginacao).map(DadosDetalhamentoUsuario::new) : this.usuarioRepository.findAllByStatus(status, paginacao).map(DadosDetalhamentoUsuario::new);
    }

    @GetMapping({"/{id}"})
    @PreAuthorize("@usuarioService.proprioUsuarioOuAdministrador(#id)")
    public ResponseEntity<DadosDetalhamentoUsuario> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(this.usuarioService.detalhar(id));
    }

    @PutMapping({"/{id}"})
    @PreAuthorize("@usuarioService.proprioUsuarioOuAdministrador(#id)")
    public ResponseEntity<DadosDetalhamentoUsuario> atualizar(@PathVariable Long id, @RequestBody @Valid DadosAtualizacaoUsuario dados) {
        DadosAtualizacaoUsuario dto = new DadosAtualizacaoUsuario(id, dados.nome(), dados.senha(), dados.email(), dados.telefone(), dados.cnpj(), dados.cpf());
        return ResponseEntity.ok(this.usuarioService.atualizar(dto));
    }

    @DeleteMapping({"/{id}"})
    @PreAuthorize("@usuarioService.proprioUsuarioOuAdministrador(#id)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        this.usuarioService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping({"/{id}/reativar"})
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        this.usuarioService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}