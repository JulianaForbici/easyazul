package easy.azul.api.dto.Security;

public record DadosRespostaLoginJWT (
    String token,
    Long id,
    String nome,
    String tipo
) {}
