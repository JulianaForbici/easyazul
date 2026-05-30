package easy.azul.api.dto.APIMapas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NominatimResponse {

    private Double lat;
    private Double lon;

    @JsonProperty("display_name")
    private String displayName;
}