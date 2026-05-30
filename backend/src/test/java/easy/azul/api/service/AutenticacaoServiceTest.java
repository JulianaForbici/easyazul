package easy.azul.api.service;

import easy.azul.api.entity.Usuario;
import easy.azul.api.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    UsuarioRepository repository;

    @InjectMocks
    AutenticacaoService autenticacaoService;

    @Test
    @DisplayName("Deve carregar usuário pelo e-mail")
    void deveRetornarUsuarioPeloEmail() {
        var usuario = mock(Usuario.class);

        when(repository.findByEmail("usuario@gmail.com")).thenReturn(Optional.of(usuario));

        var result = autenticacaoService.loadUserByUsername("usuario@gmail.com");

        assertNotNull(result);
        assertSame(usuario, result);
        verify(repository).findByEmail("usuario@gmail.com");
    }

    @Test
    @DisplayName("Não deve carregar usuário quando e-mail não existe")
    void deveLancarExcecaoQuandoNaoEncontrarEmail() {
        when(repository.findByEmail("naoexiste@gmail.com")).thenReturn(Optional.empty());

        var ex = assertThrows(UsernameNotFoundException.class, () -> autenticacaoService.loadUserByUsername("naoexiste@gmail.com"));

        assertEquals("Usuário não encontrado", ex.getMessage());
        verify(repository).findByEmail("naoexiste@gmail.com");
    }
}