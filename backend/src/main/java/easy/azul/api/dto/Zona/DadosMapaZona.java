package easy.azul.api.dto.Zona;

import easy.azul.api.entity.Enum.DisponibilidadeZona;

public record DadosMapaZona(
        Long idZona,
        String nome,
        Double latitude,
        Double longitude,
        Integer capacidadeVagas,
        Integer vagasOcupadas,
        Integer vagasDisponiveis,
        DisponibilidadeZona disponibilidade
) {}