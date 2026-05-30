package easy.azul.api.controller;

import easy.azul.api.dto.APIMapas.NominatimResponse;
import easy.azul.api.service.MapaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/localizacao")
public class LocalizacaoController {

    private final MapaService service;

    public LocalizacaoController(MapaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<NominatimResponse> buscar(
            @RequestParam String endereco
    ) {
        return ResponseEntity.ok(service.buscarPorEndereco(endereco));
    }
}