package easy.azul.api.controller;

import easy.azul.api.dto.Veiculo.DadosAtualizacaoVeiculo;
import easy.azul.api.dto.Veiculo.DadosCadastroVeiculo;
import easy.azul.api.dto.Veiculo.DadosDetalhamentoVeiculo;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.entity.Veiculo;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.VeiculoRepository;
import easy.azul.api.service.VeiculoService;
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
@RequestMapping("/veiculos")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private TicketEstacionamentoRepository ticketEstacionamentoRepository;

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('MOTORISTA', 'EMPRESA', 'ADMINISTRADOR')")
    public ResponseEntity<DadosDetalhamentoVeiculo> cadastrar(@RequestBody @Valid DadosCadastroVeiculo dados) {
        var dto = veiculoService.cadastrar(dados);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISCAL')")
    public Page<DadosDetalhamentoVeiculo> listar(@PageableDefault(size = 10, sort = {"placa"}) Pageable paginacao) {
        return veiculoRepository.findAll(paginacao).map(DadosDetalhamentoVeiculo::new);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@veiculoService.ehDonoEmpresaOuAdmin(#id)")
    public ResponseEntity<DadosDetalhamentoVeiculo> detalhar(@PathVariable Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ValidacaoException("Veículo não encontrado"));
        return ResponseEntity.ok(new DadosDetalhamentoVeiculo(veiculo));
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("@veiculoService.ehDonoEmpresaOuAdmin(#id)")
    public ResponseEntity<DadosDetalhamentoVeiculo> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid DadosAtualizacaoVeiculo dados) {

        var dto = veiculoService.atualizar(id, dados);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/meus")
    @PreAuthorize("hasAnyRole('MOTORISTA', 'EMPRESA')")
    public Page<DadosDetalhamentoVeiculo> listarMeusVeiculos(@PageableDefault(size = 10) Pageable pageable) {
        return veiculoService.listarDoUsuarioLogado(pageable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@veiculoService.ehDonoEmpresaOuAdmin(#id)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        veiculoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}