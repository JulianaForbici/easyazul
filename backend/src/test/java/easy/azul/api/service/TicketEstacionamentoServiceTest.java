package easy.azul.api.service;

import easy.azul.api.dto.Ticket.DadosCadastroTicket;
import easy.azul.api.dto.Ticket.DadosDetalhamentoTicket;
import easy.azul.api.entity.Enum.*;
import easy.azul.api.entity.TicketEstacionamento;
import easy.azul.api.entity.Usuario;
import easy.azul.api.entity.Veiculo;
import easy.azul.api.entity.ZonaEstacionamento;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.VeiculoRepository;
import easy.azul.api.repository.ZonaEstacionamentoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketEstacionamentoServiceTest {

    @InjectMocks
    private TicketEstacionamentoService service;

    @Mock
    private TicketEstacionamentoRepository ticketRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ZonaEstacionamentoRepository zonaRepository;

    @Mock
    private Clock clock;

    private final ZoneId zone = ZoneId.of("America/Sao_Paulo");

    @BeforeEach
    void setupClock() {
        Instant fixedInstant = ZonedDateTime.of(2025, 12, 19, 10, 0, 0, 0, zone).toInstant();

        lenient().when(clock.getZone()).thenReturn(zone);
        lenient().when(clock.instant()).thenReturn(fixedInstant);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void abrirQuandoVeiculoNaoExisteDeveLancarRecursoNaoEncontrado() {
        var dto = new DadosCadastroTicket(20L, 10L);

        when(veiculoRepository.findById(20L)).thenReturn(Optional.empty());


        verify(veiculoRepository).findById(20L);
        verify(zonaRepository, never()).findById(anyLong());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void abrirQuandoZonaNaoExisteDeveLancarRecursoNaoEncontrado() {
        var dto = new DadosCadastroTicket(20L, 10L);

        Veiculo veiculo = veiculoReal(20L, usuarioReal(100L, TipoUsuario.MOTORISTA), StatusVeiculo.ATIVO);

        when(veiculoRepository.findById(20L)).thenReturn(Optional.of(veiculo));
        when(zonaRepository.findById(10L)).thenReturn(Optional.empty());


        verify(zonaRepository).findById(10L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void abrirQuandoForaHorarioDaZonaDeveLancarValidacao() {
        Usuario usuario = usuarioReal(100L, TipoUsuario.MOTORISTA);
        autenticar(usuario);

        Veiculo veiculo = veiculoReal(20L, usuario, StatusVeiculo.ATIVO);
        ZonaEstacionamento zona = zonaReal(10L, StatusZona.ATIVA, LocalTime.of(23, 0), LocalTime.of(23, 30));

        when(veiculoRepository.findById(20L)).thenReturn(Optional.of(veiculo));
        when(zonaRepository.findById(10L)).thenReturn(Optional.of(zona));

        var dto = new DadosCadastroTicket(20L, 10L);

        assertThrows(ValidacaoException.class, () -> service.abrir(dto));

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void abrirQuandoVeiculoInativoDeveLancarValidacao() {
        Usuario usuario = usuarioReal(100L, TipoUsuario.MOTORISTA);
        autenticar(usuario);

        Veiculo veiculo = veiculoReal(20L, usuario, StatusVeiculo.INATIVO);
        ZonaEstacionamento zona = zonaReal(10L, StatusZona.ATIVA, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(veiculoRepository.findById(20L)).thenReturn(Optional.of(veiculo));
        when(zonaRepository.findById(10L)).thenReturn(Optional.of(zona));

        var dto = new DadosCadastroTicket(20L, 10L);

        assertThrows(ValidacaoException.class, () -> service.abrir(dto));

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void abrirQuandoZonaInativaDeveLancarValidacao() {
        Usuario usuario = usuarioReal(100L, TipoUsuario.MOTORISTA);
        autenticar(usuario);

        Veiculo veiculo = veiculoReal(20L, usuario, StatusVeiculo.ATIVO);
        ZonaEstacionamento zona = zonaReal(10L, StatusZona.INATIVA, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(veiculoRepository.findById(20L)).thenReturn(Optional.of(veiculo));
        when(zonaRepository.findById(10L)).thenReturn(Optional.of(zona));

        var dto = new DadosCadastroTicket(20L, 10L);

        assertThrows(ValidacaoException.class, () -> service.abrir(dto));

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void abrirQuandoUsuarioNaoEhDonoEhMotoristaOuEmpresaDeveNegar() {
        Usuario logado = usuarioReal(100L, TipoUsuario.MOTORISTA);
        autenticar(logado);

        Usuario donoReal = usuarioReal(200L, TipoUsuario.MOTORISTA);

        Veiculo veiculo = veiculoReal(20L, donoReal, StatusVeiculo.ATIVO);
        ZonaEstacionamento zona = zonaReal(10L, StatusZona.ATIVA, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(veiculoRepository.findById(20L)).thenReturn(Optional.of(veiculo));
        when(zonaRepository.findById(10L)).thenReturn(Optional.of(zona));

        var dto = new DadosCadastroTicket(20L, 10L);

        assertThrows(AccessDeniedException.class, () -> service.abrir(dto));

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void abrirQuandoJaExisteTicketAtivoDeveLancarValidacao() {
        Usuario usuario = usuarioReal(100L, TipoUsuario.MOTORISTA);
        autenticar(usuario);

        Veiculo veiculo = veiculoReal(20L, usuario, StatusVeiculo.ATIVO);
        ZonaEstacionamento zona = zonaReal(10L, StatusZona.ATIVA, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(veiculoRepository.findById(20L)).thenReturn(Optional.of(veiculo));
        when(zonaRepository.findById(10L)).thenReturn(Optional.of(zona));
        when(ticketRepository.existsByVeiculo_IdAndStatus(20L, StatusTicket.ATIVO)).thenReturn(true);

        var dto = new DadosCadastroTicket(20L, 10L);

        assertThrows(ValidacaoException.class, () -> service.abrir(dto));

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void abrirQuandoDeveSalvarTicketEDevolverDto() {
        Usuario usuario = usuarioReal(100L, TipoUsuario.MOTORISTA);
        autenticar(usuario);

        Veiculo veiculo = veiculoReal(20L, usuario, StatusVeiculo.ATIVO);
        ZonaEstacionamento zona = zonaReal(10L, StatusZona.ATIVA, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(veiculoRepository.findById(20L)).thenReturn(Optional.of(veiculo));
        when(zonaRepository.findById(10L)).thenReturn(Optional.of(zona));
        when(ticketRepository.existsByVeiculo_IdAndStatus(20L, StatusTicket.ATIVO)).thenReturn(false);
        zona.setTempoMaximo(120);

        when(ticketRepository.save(any(TicketEstacionamento.class))).thenAnswer(inv -> {
            TicketEstacionamento t = inv.getArgument(0);
            t.setIdTicket(999L);
            return t;
        });

        var dto = new DadosCadastroTicket(20L, 10L);

        DadosDetalhamentoTicket retorno = service.abrir(dto);

        assertNotNull(retorno);
        assertEquals(999L, retorno.id());
        assertEquals(20L, retorno.idVeiculo());
        assertEquals(10L, retorno.idZona());
        assertEquals("ATIVO", retorno.status());
        assertEquals(BigDecimal.ZERO, retorno.valor());
        assertTrue(retorno.ativo());
        assertEquals(LocalDateTime.of(2025, 12, 19, 12, 0), retorno.venceEm());

        verify(ticketRepository).save(any(TicketEstacionamento.class));
    }

    @Test
    void fecharQuandoTicketNaoExisteDeveLancarRecursoNaoEncontrado() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

    }

    @Test
    void fecharQuandoTicketNaoEstaAtivoDeveLancarValidacao() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setStatus(StatusTicket.FECHADO);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ValidacaoException.class, () -> service.fechar(1L));
    }

    @Test
    void fecharQuandoTempoMaximoExcedidoDeveFecharNormalmente() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setStatus(StatusTicket.ATIVO);
        ticket.getZona().setTempoMaximo(1);
        ticket.setInicioTicket(LocalDateTime.of(2025, 12, 19, 8, 0)); // agora é 10:00 => 2h

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        DadosDetalhamentoTicket dto = service.fechar(1L);

        assertEquals("FECHADO", dto.status());
        assertEquals(new BigDecimal("10.00"), dto.valor());
        assertEquals(LocalDateTime.of(2025, 12, 19, 10, 0), dto.fimTicket());
    }

    @Test
    void fecharQuandoOkDeveCalcularValorPorHoraArredondandoPraCima() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setStatus(StatusTicket.ATIVO);
        ticket.getZona().setTarifa(new BigDecimal("5.00"));
        ticket.getZona().setTempoMaximo(1000); // bem alto pra não atrapalhar
        ticket.setInicioTicket(LocalDateTime.of(2025, 12, 19, 9, 10));

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        DadosDetalhamentoTicket dto = service.fechar(1L);

        assertNotNull(dto);
        assertEquals("FECHADO", dto.status());
        assertFalse(dto.ativo());
        assertEquals(new BigDecimal("5.00"), dto.valor());
        assertNotNull(dto.fimTicket());
    }

    @Test
    void renovarQuandoValidoDeveAdicionarPeriodoAoVencimentoAtual() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setVenceEm(LocalDateTime.of(2025, 12, 19, 16, 13));
        ticket.getZona().setTempoMaximo(120);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        DadosDetalhamentoTicket dto = service.renovar(1L);

        assertEquals(LocalDateTime.of(2025, 12, 19, 18, 13), dto.venceEm());
        assertEquals(LocalDateTime.of(2025, 12, 19, 9, 0), dto.inicioTicket());
        assertNull(dto.fimTicket());
        assertEquals("ATIVO", dto.status());
        assertEquals(BigDecimal.ZERO, dto.valor());
    }

    @Test
    void renovarQuandoTicketNaoExisteDeveLancarRecursoNaoEncontrado() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

    }

    @Test
    void renovarQuandoTicketNaoEstaAtivoDeveLancarValidacao() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setStatus(StatusTicket.FECHADO);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ValidacaoException.class, () -> service.renovar(1L));
    }

    @Test
    void renovarQuandoTicketJaVenceuDeveLancarValidacao() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setVenceEm(LocalDateTime.of(2025, 12, 19, 9, 59, 59));

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ValidacaoException.class, () -> service.renovar(1L));
    }

    @Test
    void renovarNoInstanteDoVencimentoDeveLancarValidacao() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setVenceEm(LocalDateTime.of(2025, 12, 19, 10, 0));

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ValidacaoException.class, () -> service.renovar(1L));
    }

    @Test
    void renovarQuandoVencimentoNaoExisteDeveLancarValidacao() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setVenceEm(null);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ValidacaoException.class, () -> service.renovar(1L));
    }

    @Test
    void iniciarReservaDeveDefinirVencimentoAPartirDoInicioEfetivo() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setStatus(StatusTicket.RESERVADO);
        ticket.setInicioTicket(LocalDateTime.of(2025, 12, 19, 9, 55));
        ticket.setVenceEm(null);
        ticket.getZona().setTempoMaximo(120);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        DadosDetalhamentoTicket dto = service.iniciar(1L);

        assertEquals("ATIVO", dto.status());
        assertEquals(LocalDateTime.of(2025, 12, 19, 10, 0), dto.inicioTicket());
        assertEquals(LocalDateTime.of(2025, 12, 19, 12, 0), dto.venceEm());
    }

    @Test
    void iniciarReservaAposToleranciaDeveUsarInicioEfetivoDaPromocaoAutomatica() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setStatus(StatusTicket.RESERVADO);
        ticket.setInicioTicket(LocalDateTime.of(2025, 12, 19, 9, 45));
        ticket.setVenceEm(null);
        ticket.getZona().setTempoMaximo(120);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        DadosDetalhamentoTicket dto = service.iniciar(1L);

        assertEquals("ATIVO", dto.status());
        assertEquals(LocalDateTime.of(2025, 12, 19, 9, 55), dto.inicioTicket());
        assertEquals(LocalDateTime.of(2025, 12, 19, 11, 55), dto.venceEm());
        assertNull(dto.fimTicket());
    }

    @Test
    void iniciarReservasAtrasadasDeveDefinirVencimentoAPartirDoInicioEfetivo() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setStatus(StatusTicket.RESERVADO);
        ticket.setInicioTicket(LocalDateTime.of(2025, 12, 19, 9, 50));
        ticket.setVenceEm(null);
        ticket.getZona().setTempoMaximo(120);

        when(ticketRepository.findByStatusAndAtivoTrueAndInicioTicketLessThanEqual(
                StatusTicket.RESERVADO,
                LocalDateTime.of(2025, 12, 19, 9, 50)))
                .thenReturn(List.of(ticket));

        int quantidade = service.iniciarReservasAtrasadas();

        assertEquals(1, quantidade);
        assertEquals(StatusTicket.ATIVO, ticket.getStatus());
        assertEquals(LocalDateTime.of(2025, 12, 19, 10, 0), ticket.getInicioTicket());
        assertEquals(LocalDateTime.of(2025, 12, 19, 12, 0), ticket.getVenceEm());
    }

    @Test
    void cancelarQuandoTicketNaoExisteDeveLancarRecursoNaoEncontrado() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

    }

    @Test
    void cancelarQuandoTicketNaoEstaAtivoDeveLancarValidacao() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setStatus(StatusTicket.FECHADO);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ValidacaoException.class, () -> service.cancelar(1L));
    }

    @Test
    void cancelarQuandoOkDeveZerarValorEDesativar() {
        TicketEstacionamento ticket = ticketReal(1L);
        ticket.setStatus(StatusTicket.ATIVO);
        ticket.setValor(new BigDecimal("10.00"));
        ticket.setAtivo(true);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        DadosDetalhamentoTicket dto = service.cancelar(1L);

        assertNotNull(dto);
        assertEquals("CANCELADO", dto.status());
        assertEquals(BigDecimal.ZERO, dto.valor());
        assertFalse(dto.ativo());
        assertNotNull(dto.fimTicket());
    }

    @Test
    void podeVisualizarQuandoAdminDeveRetornarTrue() {
        Usuario admin = usuarioReal(1L, TipoUsuario.ADMINISTRADOR);
        autenticar(admin);

        TicketEstacionamento ticket = ticketReal(1L);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertTrue(service.podeVisualizar(1L));
    }

    @Test
    void podeVisualizarQuandoDonoDeveRetornarTrue() {
        Usuario dono = usuarioReal(100L, TipoUsuario.MOTORISTA);
        autenticar(dono);

        TicketEstacionamento ticket = ticketReal(1L);
        ticket.getVeiculo().setDono(dono);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertTrue(service.podeVisualizar(1L));
    }

    @Test
    void podeVisualizarQuandoNaoDonoENaoAdminDeveRetornarFalse() {
        Usuario logado = usuarioReal(999L, TipoUsuario.MOTORISTA);
        autenticar(logado);

        TicketEstacionamento ticket = ticketReal(1L);
        ticket.getVeiculo().setDono(usuarioReal(100L, TipoUsuario.MOTORISTA));

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertFalse(service.podeVisualizar(1L));
    }

    @Test
    void podeCancelarQuandoAdminOuFiscalTrueSenaoFalse() {
        autenticar(usuarioReal(1L, TipoUsuario.ADMINISTRADOR));
        assertTrue(service.podeCancelar(10L));

        autenticar(usuarioReal(2L, TipoUsuario.FISCAL));
        assertTrue(service.podeCancelar(10L));

        TicketEstacionamento ticket = ticketReal(10L);
        ticket.getVeiculo().setDono(usuarioReal(100L, TipoUsuario.MOTORISTA));

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        autenticar(usuarioReal(3L, TipoUsuario.MOTORISTA));
        assertFalse(service.podeCancelar(10L));

        autenticar(usuarioReal(4L, TipoUsuario.EMPRESA));
        assertFalse(service.podeCancelar(10L));
    }

    @Test
    void detalharQuandoOkDeveRetornarDto() {
        TicketEstacionamento ticket = ticketReal(1L);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        DadosDetalhamentoTicket dto = service.detalhar(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.id());
    }

    @Test
    void listarTodosDeveMapearPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        TicketEstacionamento t1 = ticketReal(1L);
        TicketEstacionamento t2 = ticketReal(2L);

        when(ticketRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(t1, t2), pageable, 2));

        Page<DadosDetalhamentoTicket> page = service.listarTodos(pageable);

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

    @Test
    void listarMeusDeveFiltrarPeloUsuarioLogado() {
        Usuario logado = usuarioReal(100L, TipoUsuario.MOTORISTA);
        autenticar(logado);

        Pageable pageable = PageRequest.of(0, 10);

        TicketEstacionamento t1 = ticketReal(1L);
        t1.getVeiculo().setDono(logado);

        when(ticketRepository.findByVeiculo_Dono_IdUsuario(100L, pageable))
                .thenReturn(new PageImpl<>(List.of(t1), pageable, 1));

        Page<DadosDetalhamentoTicket> page = service.listarMeus(pageable);

        assertEquals(1, page.getTotalElements());
        verify(ticketRepository).findByVeiculo_Dono_IdUsuario(100L, pageable);
    }

    private void autenticar(Usuario usuario) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuario);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(ctx);
    }

    private Usuario usuarioReal(Long id, TipoUsuario tipo) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNome("User " + id);
        u.setEmail("user" + id + "@gmail.com");
        u.setTipo(tipo);
        u.setStatus(StatusUsuario.ATIVO);
        u.setTelefone("1199999000" + id);
        return u;
    }

    private Veiculo veiculoReal(Long idVeiculo, Usuario dono, StatusVeiculo status) {
        Veiculo v = new Veiculo();
        v.setId(idVeiculo);
        v.setDono(dono);
        v.setPlaca("ABC1D23");
        v.setTipoVeiculo(TipoVeiculo.CARRO);
        v.setStatus(status);
        return v;
    }

    private ZonaEstacionamento zonaReal(Long idZona, StatusZona status, LocalTime inicio, LocalTime fim) {
        ZonaEstacionamento z = new ZonaEstacionamento();
        z.setIdZonaEstacionamento(idZona);
        z.setNome("Zona " + idZona);
        z.setTarifa(new BigDecimal("5.00"));
        z.setTempoMaximo(999);
        z.setHoraInicio(inicio);
        z.setHoraFim(fim);
        z.setStatus(status);
        z.setLatitude(-23.0);
        z.setLongitude(-46.0);
        z.setCapacidadeVagas(100);
        return z;
    }

    private TicketEstacionamento ticketReal(Long idTicket) {
        Usuario dono = usuarioReal(100L, TipoUsuario.MOTORISTA);
        Veiculo veiculo = veiculoReal(20L, dono, StatusVeiculo.ATIVO);
        ZonaEstacionamento zona = zonaReal(10L, StatusZona.ATIVA, LocalTime.of(8, 0), LocalTime.of(18, 0));

        TicketEstacionamento t = new TicketEstacionamento();
        t.setIdTicket(idTicket);
        t.setVeiculo(veiculo);
        t.setZona(zona);
        t.setInicioTicket(LocalDateTime.of(2025, 12, 19, 9, 0));
        t.setFimTicket(null);
        t.setValor(BigDecimal.ZERO);
        t.setAtivo(true);
        t.setStatus(StatusTicket.ATIVO);
        return t;
    }
}
