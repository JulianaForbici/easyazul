package easy.azul.api.controller;

import easy.azul.api.dto.Security.DadosAutenticacaoJWT;
import easy.azul.api.dto.Security.DadosRespostaLoginJWT;
import easy.azul.api.infra.security.TokenService;
import easy.azul.api.entity.Usuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class UsuarioLogin {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody @Valid DadosAutenticacaoJWT dados) {
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());

            var authentication = authenticationManager.authenticate(authenticationToken);

            var usuario = (Usuario) authentication.getPrincipal();
            var token = tokenService.gerarToken(usuario);

            return ResponseEntity.ok(
                    new DadosRespostaLoginJWT(token, usuario.getIdUsuario(), usuario.getNome(), usuario.getTipo().name()));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("E-mail ou senha inválidos");
        }
    }
}