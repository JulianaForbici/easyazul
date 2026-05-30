package easy.azul.api.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import easy.azul.api.entity.Usuario;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("API easy.azul.api")
                    .withSubject(usuario.getEmail())
                    .withClaim("role", usuario.getTipo().name())
                    .withExpiresAt(dataExpiracao())
                    .sign(algoritmo);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer("API easy.azul.api")
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }

    public String getRole(String tokenJWT) {
        var algoritmo = Algorithm.HMAC256(secret);

        return JWT.require(algoritmo)
                .withIssuer("API easy.azul.api")
                .build()
                .verify(tokenJWT)
                .getClaim("role")
                .asString();
    }

    @PostConstruct
    public void init() {
        String s = secret == null ? "null" : secret;
        String masked = s.length() <= 4 ? "****" : "****" + s.substring(s.length()-4);
        System.out.println("[DEBUG] Token secret (masked): " + masked);
    }

    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

}