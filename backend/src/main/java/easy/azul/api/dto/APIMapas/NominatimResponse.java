package easy.azul.api.dto.APIMapas;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NominatimResponse {

    private Double lat;
    private Double lon;
    private String display_name;

    public java.lang.Double getLat() {
        return this.lat;
    }

    public java.lang.Double getLon() {
        return this.lon;
    }

    public java.lang.String getDisplay_name() {
        return this.display_name;
    }
}
