package easy.azul.api.service;

import easy.azul.api.dto.Veiculo.DadosAtualizacaoVeiculo;
import easy.azul.api.dto.Veiculo.DadosCadastroVeiculo;
import easy.azul.api.dto.Veiculo.DadosDetalhamentoVeiculo;
import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.Enum.StatusUsuario;
import easy.azul.api.entity.Enum.StatusVeiculo;
import easy.azul.api.entity.Enum.TipoUsuario;
import easy.azul.api.entity.Enum.TipoVeiculo;
import easy.azul.api.entity.Usuario;
import easy.azul.api.entity.Veiculo;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.UsuarioRepository;
import easy.azul.api.repository.VeiculoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock private VeiculoRepository veiculoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TicketEstacionamentoRepository ticketEstacionamentoRepository;

    @InjectMocks private VeiculoService service;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Usuario usuario(Long id, TipoUsuario tipo, StatusUsuario status) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setTipo(tipo);
        u.setStatus(status);
        u.setNome("User " + id);
        u.setEmail("u" + id + "@mail.com");
        u.setTelefone("999999999");
        u.setSenha("x");
        return u;
    }

    private Veiculo veiculo(Long id, String placa, Usuario dono, TipoVeiculo tipoVeiculo, StatusVeiculo status) {
        Veiculo v = new Veiculo();
        v.setId(id);
        v.setPlaca(placa);
        v.setDono(dono);
        v.setTipoVeiculo(tipoVeiculo);
        v.setStatus(status);
        return v;
    }

    private void logarComo(Usuario usuario) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);
    }

    @Test
    void cadastrarQuandoNaoAdminETentaCadastrarParaOutroUsuarioDeveLancarValidacao() {
        Usuario logado = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(logado);

        DadosCadastroVeiculo dados = new DadosCadastroVeiculo("ABC1D23", 2L, TipoVeiculo.CARRO);

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));
        assertTrue(ex.getMessage().toLowerCase().contains("não pode cadastrar veículo para outro usuário".toLowerCase()));

        verifyNoInteractions(veiculoRepository, usuarioRepository, ticketEstacionamentoRepository);
    }

    @Test
    void cadastrarQuandoPlacaJaExisteDeveLancarValidacao() {
        Usuario logado = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(logado);

        DadosCadastroVeiculo dados = new DadosCadastroVeiculo(" abc1d23 ", 1L, TipoVeiculo.CARRO);

        when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(true);

        assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        verify(veiculoRepository).existsByPlaca("ABC1D23");
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(usuarioRepository, ticketEstacionamentoRepository);
    }

    @Test
    void cadastrarQuandoDonoInativoDeveLancarValidacao() {
        Usuario logado = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(logado);

        DadosCadastroVeiculo dados = new DadosCadastroVeiculo("ABC1D23", 1L, TipoVeiculo.CARRO);

        when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);

        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.INATIVO);
        when(usuarioRepository.getReferenceById(1L)).thenReturn(dono);

        assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        verify(veiculoRepository).existsByPlaca("ABC1D23");
        verify(usuarioRepository).getReferenceById(1L);
        verify(veiculoRepository, never()).save(any());
        verifyNoInteractions(ticketEstacionamentoRepository);
    }

    @Test
    void cadastrarQuandoDonoEhAdministradorDeveLancarValidacao() {
        Usuario logado = usuario(99L, TipoUsuario.ADMINISTRADOR, StatusUsuario.ATIVO);
        logarComo(logado);

        DadosCadastroVeiculo dados = new DadosCadastroVeiculo("ABC1D23", 10L, TipoVeiculo.CARRO);

        when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);

        Usuario donoAdmin = usuario(10L, TipoUsuario.ADMINISTRADOR, StatusUsuario.ATIVO);
        when(usuarioRepository.getReferenceById(10L)).thenReturn(donoAdmin);

        assertThrows(ValidacaoException.class, () -> service.cadastrar(dados));

        verify(usuarioRepository).getReferenceById(10L);
        verify(veiculoRepository, never()).save(any());
        verifyNoInteractions(ticketEstacionamentoRepository);
    }

    @Test
    void cadastrarMtoristaCadastrandoParaSiDeveSalvarComPlacaNormalizadaEStatusAtivo() {
        Usuario logado = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(logado);

        DadosCadastroVeiculo dados = new DadosCadastroVeiculo("  abc1d23  ", 1L, TipoVeiculo.MOTO);

        when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);

        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        when(usuarioRepository.getReferenceById(1L)).thenReturn(dono);

        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> {
            Veiculo v = inv.getArgument(0);
            v.setId(10L);
            return v;
        });

        DadosDetalhamentoVeiculo resp = service.cadastrar(dados);

        assertNotNull(resp);
        assertEquals(10L, resp.id());
        assertEquals("ABC1D23", resp.placa());
        assertEquals(1L, resp.idDono());
        assertEquals("User 1", resp.nomeDono());
        assertEquals(TipoVeiculo.MOTO, resp.tipoVeiculo());

        ArgumentCaptor<Veiculo> captor = ArgumentCaptor.forClass(Veiculo.class);
        verify(veiculoRepository).save(captor.capture());

        Veiculo salvo = captor.getValue();
        assertEquals("ABC1D23", salvo.getPlaca());
        assertEquals(StatusVeiculo.ATIVO, salvo.getStatus());
        assertEquals(dono, salvo.getDono());
        assertEquals(TipoVeiculo.MOTO, salvo.getTipoVeiculo());
    }

    @Test
    void cadastrarQuandoAdminPodeCadastrarParaOutroUsuarioDeveSalvar() {
        Usuario admin = usuario(99L, TipoUsuario.ADMINISTRADOR, StatusUsuario.ATIVO);
        logarComo(admin);

        DadosCadastroVeiculo dados = new DadosCadastroVeiculo("ABC1D23", 5L, TipoVeiculo.CARRO);

        when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);

        Usuario dono = usuario(5L, TipoUsuario.EMPRESA, StatusUsuario.ATIVO);
        when(usuarioRepository.getReferenceById(5L)).thenReturn(dono);

        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        DadosDetalhamentoVeiculo resp = service.cadastrar(dados);

        assertNotNull(resp);
        assertEquals("ABC1D23", resp.placa());
        assertEquals(5L, resp.idDono());
    }

    @Test
    void atualizarQuandoVeiculoNaoExisteDeveLancarValidacao() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        DadosAtualizacaoVeiculo dados = new DadosAtualizacaoVeiculo(null, "ABC1D23", TipoVeiculo.CARRO);

        assertThrows(ValidacaoException.class, () -> service.atualizar(1L, dados));
        verify(veiculoRepository).findById(1L);
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(usuarioRepository, ticketEstacionamentoRepository);
    }

    @Test
    void atualizarQuandoVeiculoInativoDeveLancarValidacao() {
        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.INATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));

        DadosAtualizacaoVeiculo dados = new DadosAtualizacaoVeiculo(null, "DEF1G23", TipoVeiculo.MOTO);

        assertThrows(ValidacaoException.class, () -> service.atualizar(10L, dados));
        verify(veiculoRepository).findById(10L);
        verifyNoInteractions(usuarioRepository, ticketEstacionamentoRepository);
    }

    @Test
    void atualizarQuandoNaoAdminENaoDonoDeveNegar() {
        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.ATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));

        Usuario outro = usuario(2L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(outro);

        DadosAtualizacaoVeiculo dados = new DadosAtualizacaoVeiculo(null, "DEF1G23", null);

        assertThrows(AccessDeniedException.class, () -> service.atualizar(10L, dados));
        verify(veiculoRepository).findById(10L);
        verifyNoInteractions(usuarioRepository, ticketEstacionamentoRepository);
    }

    @Test
    void atualizarQuandoPlacaNovaJaExisteDeveLancarValidacao() {
        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.ATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));
        logarComo(dono);

        when(veiculoRepository.existsByPlaca("DEF1G23")).thenReturn(true);

        DadosAtualizacaoVeiculo dados = new DadosAtualizacaoVeiculo(null, " def1g23 ", null);

        assertThrows(ValidacaoException.class, () -> service.atualizar(10L, dados));
        verify(veiculoRepository).existsByPlaca("DEF1G23");
        verifyNoInteractions(usuarioRepository, ticketEstacionamentoRepository);
    }

    @Test
    void atualizarDonoAtualizaPlacaETipoDeveAlterarNoObjetoRetornado() {
        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.ATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));
        logarComo(dono);

        when(veiculoRepository.existsByPlaca("DEF1G23")).thenReturn(false);

        DadosAtualizacaoVeiculo dados = new DadosAtualizacaoVeiculo(null, " def1g23 ", TipoVeiculo.MOTO);

        DadosDetalhamentoVeiculo resp = service.atualizar(10L, dados);

        assertEquals(10L, resp.id());
        assertEquals("DEF1G23", resp.placa());
        assertEquals(TipoVeiculo.MOTO, resp.tipoVeiculo());
    }

    @Test
    void listarDoUsuarioLogadoDeveBuscarSomenteAtivosDoUsuario() {
        Usuario logado = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(logado);

        Pageable pageable = PageRequest.of(0, 10);

        Veiculo v1 = veiculo(1L, "ABC1D23", logado, TipoVeiculo.CARRO, StatusVeiculo.ATIVO);
        Page<Veiculo> page = new PageImpl<>(java.util.List.of(v1), pageable, 1);

        when(veiculoRepository.findByDonoIdUsuarioAndStatus(1L, StatusVeiculo.ATIVO, pageable)).thenReturn(page);

        Page<DadosDetalhamentoVeiculo> resp = service.listarDoUsuarioLogado(pageable);

        assertEquals(1, resp.getTotalElements());
        assertEquals("ABC1D23", resp.getContent().get(0).placa());

        verify(veiculoRepository).findByDonoIdUsuarioAndStatus(1L, StatusVeiculo.ATIVO, pageable);
        verifyNoInteractions(usuarioRepository, ticketEstacionamentoRepository);
    }

    @Test
    void quandoAdminDeveRetornarTrueSemConsultarRepositorio() {
        Usuario admin = usuario(99L, TipoUsuario.ADMINISTRADOR, StatusUsuario.ATIVO);
        logarComo(admin);

        assertTrue(service.ehDonoEmpresaOuAdmin(10L));

        verifyNoInteractions(veiculoRepository, usuarioRepository, ticketEstacionamentoRepository);
    }

    @Test
    void quandoVeiculoNaoExisteDeveLancarValidacao() {
        Usuario logado = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(logado);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ValidacaoException.class, () -> service.ehDonoEmpresaOuAdmin(10L));
        verify(veiculoRepository).findById(10L);
    }

    @Test
    void quandoVeiculoInativoDeveRetornarFalse() {
        Usuario logado = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(logado);

        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.INATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));

        assertFalse(service.ehDonoEmpresaOuAdmin(10L));
        verify(veiculoRepository).findById(10L);
    }

    @Test
    void quandoDonoDeveRetornarTrue() {
        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(dono);

        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.ATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));

        assertTrue(service.ehDonoEmpresaOuAdmin(10L));
        verify(veiculoRepository).findById(10L);
    }

    @Test
    void quandoNaoDonoDeveRetornarFalse() {
        Usuario logado = usuario(2L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        logarComo(logado);

        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.ATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));

        assertFalse(service.ehDonoEmpresaOuAdmin(10L));
        verify(veiculoRepository).findById(10L);
    }

    @Test
    void excluirDeveLancarValidacaoQuandoVeiculoNaoExiste() {
        when(veiculoRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ValidacaoException.class, () -> service.excluir(10L));
        verify(veiculoRepository).findById(10L);
        verifyNoInteractions(ticketEstacionamentoRepository, usuarioRepository);
    }

    @Test
    void excluirDeveLancarValidacaoComVeiculoAtv() {
        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.INATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));

        assertThrows(ValidacaoException.class, () -> service.excluir(10L));

        verify(veiculoRepository).findById(10L);
        verifyNoInteractions(ticketEstacionamentoRepository, usuarioRepository);
    }

    @Test
    void excluirDeveLancarValidacaoComTicketAtv() {
        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.ATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));
        when(ticketEstacionamentoRepository.existsByVeiculo_IdAndStatus(10L, StatusTicket.ATIVO)).thenReturn(true);

        assertThrows(ValidacaoException.class, () -> service.excluir(10L));

        verify(ticketEstacionamentoRepository).existsByVeiculo_IdAndStatus(10L, StatusTicket.ATIVO);
        assertEquals(StatusVeiculo.ATIVO, v.getStatus());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void excluirDeveMarcarComoInativo() {
        Usuario dono = usuario(1L, TipoUsuario.MOTORISTA, StatusUsuario.ATIVO);
        Veiculo v = veiculo(10L, "ABC1D23", dono, TipoVeiculo.CARRO, StatusVeiculo.ATIVO);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(v));
        when(ticketEstacionamentoRepository.existsByVeiculo_IdAndStatus(10L, StatusTicket.ATIVO)).thenReturn(false);

        service.excluir(10L);

        assertEquals(StatusVeiculo.INATIVO, v.getStatus());
        verify(ticketEstacionamentoRepository).existsByVeiculo_IdAndStatus(10L, StatusTicket.ATIVO);
        verifyNoInteractions(usuarioRepository);
    }
}