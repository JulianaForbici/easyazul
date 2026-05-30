package easy.azul.api.controller;

import easy.azul.api.dto.Usuario.DadosAtualizacaoUsuario;
import easy.azul.api.dto.Usuario.DadosCadastroUsuario;
import easy.azul.api.dto.Usuario.DadosDetalhamentoUsuario;
import easy.azul.api.entity.Enum.StatusUsuario;
import easy.azul.api.entity.Usuario;
import easy.azul.api.repository.UsuarioRepository;
import easy.azul.api.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioController controller;

    @BeforeEach
    void setup() {
        controller = new UsuarioController(usuarioService, usuarioRepository);
    }

    @Test
    void cadastrarDeveRetornarCreatedComLocationEBody() {
        var dados = mock(DadosCadastroUsuario.class);
        var dto = mock(DadosDetalhamentoUsuario.class);
        when(dto.id()).thenReturn(123L);
        when(usuarioService.cadastrar(dados)).thenReturn(dto);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        ResponseEntity<DadosDetalhamentoUsuario> response =
                controller.cadastrar(dados, uriBuilder);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
        assertEquals("http://localhost/usuarios/123", response.getHeaders().getFirst(HttpHeaders.LOCATION));

        verify(usuarioService).cadastrar(dados);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void cadastrarAdminDeveRetornarCreated() {
        var dados = mock(DadosCadastroUsuario.class);
        var dto = mock(DadosDetalhamentoUsuario.class);
        when(dto.id()).thenReturn(50L);
        when(usuarioService.cadastrarAdmin(dados)).thenReturn(dto);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        ResponseEntity<DadosDetalhamentoUsuario> response = controller.cadastrarAdmin(dados, uriBuilder);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(usuarioService).cadastrarAdmin(dados);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void listarSemStatusDeveBuscarTodos() {
        Pageable pageable = PageRequest.of(0, 10);

        Usuario usuario = mock(Usuario.class);
        Page<Usuario> page = new PageImpl<>(List.of(usuario), pageable, 1);

        when(usuarioRepository.findAll(pageable)).thenReturn(page);

        Page<DadosDetalhamentoUsuario> result = controller.listar(null, pageable);

        assertEquals(1, result.getTotalElements());

        verify(usuarioRepository).findAll(pageable);
        verifyNoInteractions(usuarioService);
    }

    @Test
    void listarComStatusDeveBuscarPorStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Usuario usuario = mock(Usuario.class);
        Page<Usuario> page = new PageImpl<>(List.of(usuario), pageable, 1);

        when(usuarioRepository.findAllByStatus(StatusUsuario.INATIVO, pageable)).thenReturn(page);

        Page<DadosDetalhamentoUsuario> result = controller.listar(StatusUsuario.INATIVO, pageable);

        assertEquals(1, result.getTotalElements());

        verify(usuarioRepository).findAllByStatus(StatusUsuario.INATIVO, pageable);
        verifyNoInteractions(usuarioService);
    }

    @Test
    void detalharDeveDelegarParaService() {
        Long id = 10L;
        var dto = mock(DadosDetalhamentoUsuario.class);

        when(usuarioService.detalhar(id)).thenReturn(dto);

        ResponseEntity<DadosDetalhamentoUsuario> response = controller.detalhar(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(usuarioService).detalhar(id);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void atualizarDeveMontarDtoComIdCorreto() {
        Long id = 77L;

        var dados = mock(DadosAtualizacaoUsuario.class);
        when(dados.nome()).thenReturn("Juliana");
        when(dados.email()).thenReturn("ju@gmail.com");
        when(dados.telefone()).thenReturn("11999999999");
        when(dados.cnpj()).thenReturn("28329860000192");
        when(dados.cpf()).thenReturn(null);
        when(dados.senha()).thenReturn("123456");

        var retorno = mock(DadosDetalhamentoUsuario.class);
        when(usuarioService.atualizar(any())).thenReturn(retorno);

        ResponseEntity<DadosDetalhamentoUsuario> response = controller.atualizar(id, dados);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<DadosAtualizacaoUsuario> captor = ArgumentCaptor.forClass(DadosAtualizacaoUsuario.class);

        verify(usuarioService).atualizar(captor.capture());

        DadosAtualizacaoUsuario enviado = captor.getValue();
        assertEquals(id, enviado.id());
        assertEquals("Juliana", enviado.nome());
        assertEquals("ju@gmail.com", enviado.email());
        assertEquals("11999999999", enviado.telefone());
        assertEquals("28329860000192", enviado.cnpj());
        assertNull(enviado.cpf());
        assertEquals("123456", enviado.senha());
    }

    @Test
    void excluirDeveInativarUsuario() {
        Long id = 9L;

        ResponseEntity<Void> response = controller.excluir(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(usuarioService).inativar(id);
        verifyNoInteractions(usuarioRepository);
    }
}