package easy.azul.api.service;

import easy.azul.api.client.MapaClient;
import easy.azul.api.dto.APIMapas.NominatimResponse;
import easy.azul.api.infra.exception.ValidacaoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapaService {

    private final MapaClient mapaClient;

    public MapaService(MapaClient mapaClient) {
        this.mapaClient = mapaClient;
    }

    public NominatimResponse buscarPorEndereco(String endereco) {

        List<NominatimResponse> resposta = mapaClient.buscarEndereco(endereco);

        if (resposta == null || resposta.isEmpty()) {
            throw new ValidacaoException("Endereço não encontrado");
        }

        return resposta.get(0);
    }
}