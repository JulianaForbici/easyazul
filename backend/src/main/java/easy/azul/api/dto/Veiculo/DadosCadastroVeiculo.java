package easy.azul.api.dto.Veiculo;

import easy.azul.api.entity.Enum.TipoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DadosCadastroVeiculo (
        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$", message = "Placa inválida")
        String placa,

        @NotNull
        Long idDono,

        @NotNull
        TipoVeiculo tipoVeiculo
){}
