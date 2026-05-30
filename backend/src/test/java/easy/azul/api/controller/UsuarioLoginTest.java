package easy.azul.api.controller;

import easy.azul.api.dto.Security.DadosAutenticacaoJWT;
import easy.azul.api.dto.Security.DadosRespostaLoginJWT;
import easy.azul.api.entity.Enum.TipoUsuario;
import easy.azul.api.entity.Usuario;
import easy.azul.api.infra.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioLoginTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UsuarioLogin controller;

    @Test
    void loginComCredenciaisValidasDeveRetornar200ComTokenEDados() {
        var dados = mock(DadosAutenticacaoJWT.class);
        when(dados.email()).thenReturn("ju@gmail.com");
        when(dados.senha()).thenReturn("123456");

        var usuario = mock(Usuario.class);
        when(usuario.getIdUsuario()).thenReturn(10L);
        when(usuario.getNome()).thenReturn("Juliana");
        when(usuario.getTipo()).thenReturn(TipoUsuario.MOTORISTA);

        when(authentication.getPrincipal()).thenReturn(usuario);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(tokenService.gerarToken(usuario)).thenReturn("token-jwt");

        ResponseEntity<?> response = controller.login(dados);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(DadosRespostaLoginJWT.class, response.getBody());

        DadosRespostaLoginJWT body = (DadosRespostaLoginJWT) response.getBody();
        assertEquals("token-jwt", body.token());
        assertEquals(10L, body.id());
        assertEquals("Juliana", body.nome());
        assertEquals("MOTORISTA", body.tipo());

        verify(authenticationManager).authenticate(argThat(authToken
                -> authToken.getPrincipal().equals("ju@gmail.com") && authToken.getCredentials().equals("123456")));
        verify(tokenService).gerarToken(usuario);
        verifyNoMoreInteractions(tokenService, authenticationManager);
    }

    @Test
    void loginComCredenciaisInvalidasDeveRetornar401ComMensagem() {
        var dados = mock(DadosAutenticacaoJWT.class);
        when(dados.email()).thenReturn("ju@gmail.com");
        when(dados.senha()).thenReturn("senha-errada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        ResponseEntity<?> response = controller.login(dados);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("E-mail ou senha inválidos", response.getBody());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(tokenService);
    }
}