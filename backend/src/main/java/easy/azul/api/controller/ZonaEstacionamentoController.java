package easy.azul.api.controller;

import easy.azul.api.dto.Zona.DadosAtualizacaoZona;
import easy.azul.api.dto.Zona.DadosCadastroZona;
import easy.azul.api.dto.Zona.DadosDetalhamentoZona;
import easy.azul.api.dto.Zona.DadosPinZona;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.entity.Enum.StatusZona;
import easy.azul.api.entity.ZonaEstacionamento;
import easy.azul.api.repository.ZonaEstacionamentoRepository;
import easy.azul.api.service.ZonaEstacionamentoService;
import easy.azul.api.service.ZonaMapaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/zonas")
public class ZonaEstacionamentoController {

    @Autowired
    private ZonaEstacionamentoService zonaService;

    @Autowired
    private ZonaEstacionamentoRepository zonaEstacionamentoRepository;

    @Autowired
    private ZonaMapaService zonaMapaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DadosDetalhamentoZona> cadastrar(@RequestBody @Valid DadosCadastroZona dados) {
        var dto = zonaService.cadastrar(dados);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public Page<DadosDetalhamentoZona> listar(
            @PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {

        return zonaEstacionamentoRepository
                .findByStatus(StatusZona.ATIVA, paginacao)
                .map(DadosDetalhamentoZona::new);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoZona> detalhar(@PathVariable Long id) {
        ZonaEstacionamento zona = zonaEstacionamentoRepository.findById(id)
                .orElseThrow(() -> new ValidacaoException("Zona não encontrada"));

        if (zona.getStatus() != StatusZona.ATIVA) {
            throw new ValidacaoException("Zona inativa.");
        }

        return ResponseEntity.ok(new DadosDetalhamentoZona(zona));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DadosDetalhamentoZona> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid DadosAtualizacaoZona dados) {

        var dto = zonaService.atualizar(id, dados);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        zonaService.excluir(id); // exclusão lógica
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<DadosDetalhamentoZona> listarTodas(
            @PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {

        return zonaEstacionamentoRepository.findAll(paginacao)
                .map(DadosDetalhamentoZona::new);
    }

    @GetMapping("/pins")
    public ResponseEntity<List<DadosPinZona>> pins() {
        return ResponseEntity.ok(zonaMapaService.listarPins());
    }

    @GetMapping("/pins/proximas")
    public ResponseEntity<List<DadosPinZona>> proximas(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "2000") Double raio
    ) {
        return ResponseEntity.ok(zonaMapaService.listarPinsProximas(lat, lon, raio));
    }
}