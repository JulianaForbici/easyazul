package easy.azul.api.dto.Zona;

public record DadosPinZona(
        Long idZona,
        String nome,
        Double latitude,
        Double longitude,
        Integer capacidade,
        Long ocupadas,
        Long disponiveis
) {}