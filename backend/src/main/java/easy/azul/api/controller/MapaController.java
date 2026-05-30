package easy.azul.api.controller;

import easy.azul.api.dto.Zona.DadosMapaZona;
import easy.azul.api.service.MapaZonaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mapa")
public class MapaController {

    private final MapaZonaService mapaZonaService;

    public MapaController(MapaZonaService mapaZonaService) {
        this.mapaZonaService = mapaZonaService;
    }

    @GetMapping("/zonas")
    public List<DadosMapaZona> listarZonas() {
        return mapaZonaService.listarZonasMapa();
    }
}