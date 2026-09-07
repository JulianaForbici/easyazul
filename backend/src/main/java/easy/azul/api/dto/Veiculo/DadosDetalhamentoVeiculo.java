package easy.azul.api.dto.Veiculo;
import easy.azul.api.entity.Veiculo;

import easy.azul.api.entity.Enum.StatusVeiculo;
import easy.azul.api.entity.Enum.TipoVeiculo;
import easy.azul.api.entity.Veiculo;

public record DadosDetalhamentoVeiculo (
        Long id,
        String placa,
        Long idDono,
        String nomeDono,
        TipoVeiculo tipoVeiculo,
        StatusVeiculo statusVeiculo
){
    public DadosDetalhamentoVeiculo(Veiculo veiculo) {
        this(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getDono().getIdUsuario(),
                veiculo.getDono().getNome(),
                veiculo.getTipoVeiculo(),
                veiculo.getStatus());
    }
}