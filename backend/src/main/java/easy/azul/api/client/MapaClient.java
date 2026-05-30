package easy.azul.api.client;

import easy.azul.api.dto.APIMapas.NominatimResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class MapaClient {

    private final WebClient webClient;

    public MapaClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "easy-azul-api")
                .build();
    }

    public List<NominatimResponse> buscarEndereco(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search")
                        .queryParam("q", query).queryParam("format", "json").build())
                .retrieve()
                .bodyToFlux(NominatimResponse.class)
                .collectList()
                .block();
    }
}