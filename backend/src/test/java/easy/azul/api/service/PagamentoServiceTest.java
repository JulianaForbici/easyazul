package easy.azul.api.service;

import easy.azul.api.dto.Pagamento.DadosCadastroPagamento;
import easy.azul.api.dto.Pagamento.DadosDetalhamentoPagamento;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.entity.Enum.StatusPagamento;
import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.Enum.TipoUsuario;
import easy.azul.api.entity.Pagamento;
import easy.azul.api.entity.TicketEstacionamento;
import easy.azul.api.entity.Usuario;
import easy.azul.api.entity.Veiculo;
import easy.azul.api.repository.PagamentoRepository;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static easy.azul.api.entity.Enum.TipoUsuario.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock PagamentoRepository pagamentoRepository;
    @Mock TicketEstacionamentoRepository ticketRepository;
    @Mock Clock clock;

    @InjectMocks PagamentoService pagamentoService;

    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        lenient().when(clock.instant()).thenReturn(Instant.parse("2025-12-18T10:15:30Z"));
        lenient().when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(i -> i.getArgument(0));
    }

    private void autenticar(Usuario usuarioLogado) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuarioLogado);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(ctx);
    }

    private Usuario usuario(long id, TipoUsuario tipo) {
        var u = mock(Usuario.class);
        lenient().when(u.getIdUsuario()).thenReturn(id);
        lenient().when(u.getTipo()).thenReturn(tipo);
        return u;
    }

    private TicketEstacionamento ticket(long idTicket, StatusTicket status, long idDono, BigDecimal valor) {
        var ticket = mock(TicketEstacionamento.class);

        var dono = mock(Usuario.class);
        lenient().when(dono.getIdUsuario()).thenReturn(idDono);

        var veiculo = mock(Veiculo.class);
        lenient().when(veiculo.getDono()).thenReturn(dono);

        lenient().when(ticket.getIdTicket()).thenReturn(idTicket);
        lenient().when(ticket.getStatus()).thenReturn(status);
        lenient().when(ticket.getValor()).thenReturn(valor);
        lenient().when(ticket.getVeiculo()).thenReturn(veiculo);

        return ticket;
    }

    private DadosCadastroPagamento dados(long ticketId) {
        var d = mock(DadosCadastroPagamento.class);
        lenient().when(d.idTicket()).thenReturn(ticketId);
        lenient().when(d.codigoReferencia()).thenReturn("REF-" + ticketId);
        lenient().when(d.observacao()).thenReturn("obs");
        lenient().when(d.formaPagamento()).thenReturn(null);
        return d;
    }

    private Pagamento pagamento(StatusPagamento status, long idUsuarioPagamento, StatusTicket statusTicket) {
        var p = new Pagamento();

        var u = mock(Usuario.class);
        lenient().when(u.getIdUsuario()).thenReturn(idUsuarioPagamento);
        p.setUsuario(u);

        var t = mock(TicketEstacionamento.class);
        lenient().when(t.getStatus()).thenReturn(statusTicket);
        p.setTicket(t);

        p.setStatus(status);
        p.setValor(new BigDecimal("10.00"));
        return p;
    }

    @Test
    @DisplayName("criarDeveCriarPagamentoPendente")
    void criarDeveCriarPagamentoPendente() {
        var logado = usuario(1L, MOTORISTA);
        autenticar(logado);

        var dados = dados(10L);
        var ticket = ticket(10L, StatusTicket.FECHADO, 1L, new BigDecimal("12.50"));

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(pagamentoRepository.existsByTicket_IdTicketAndStatusIn(eq(10L), anyList())).thenReturn(false);

        assertNotNull(pagamentoService.criar(dados));
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("criarDeveLancarNaoEncontradoQuandoTicketNaoExiste")
    void criarDeveLancarNaoEncontradoQuandoTicketNaoExiste() {
        var logado = usuario(1L, MOTORISTA);
        autenticar(logado);

        var dados = dados(10L);

        when(ticketRepository.findById(10L)).thenReturn(Optional.empty());

        assertEquals("Ticket não encontrado!", ex.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = StatusTicket.class, names = "FECHADO", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("criarDeveBloquearTicketNaoFechado")
    void criarDeveBloquearTicketNaoFechado(StatusTicket status) {
        var logado = usuario(1L, MOTORISTA);
        autenticar(logado);

        var dados = dados(10L);
        var ticket = ticket(10L, status, 1L, new BigDecimal("12.50"));

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        var ex = assertThrows(ValidacaoException.class, () -> pagamentoService.criar(dados));
        assertEquals("Só é possível pagar ticket FECHADO!", ex.getMessage());
    }

    @Test
    @DisplayName("criarDeveBloquearTicketDeOutroUsuario")
    void criarDeveBloquearTicketDeOutroUsuario() {
        var logado = usuario(1L, MOTORISTA);
        autenticar(logado);

        var dados = dados(10L);
        var ticket = ticket(10L, StatusTicket.FECHADO, 2L, new BigDecimal("12.50"));

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        var ex = assertThrows(AccessDeniedException.class, () -> pagamentoService.criar(dados));
        assertEquals("Você não pode pagar ticket de outro usuário!", ex.getMessage());
    }

    @Test
    @DisplayName("criarDeveBloquearSeJaExistePagamento")
    void criarDeveBloquearSeJaExistePagamento() {
        var logado = usuario(1L, MOTORISTA);
        autenticar(logado);

        var dados = dados(10L);
        var ticket = ticket(10L, StatusTicket.FECHADO, 1L, new BigDecimal("12.50"));

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(pagamentoRepository.existsByTicket_IdTicketAndStatusIn(eq(10L), anyList())).thenReturn(true);

        var ex = assertThrows(ValidacaoException.class, () -> pagamentoService.criar(dados));
        assertEquals("Já existe pagamento para este ticket!", ex.getMessage());
    }

    @Test
    @DisplayName("confirmarDeveAprovarPagamentoPendente")
    void confirmarDeveAprovarPagamentoPendente() {
        var t = mock(TicketEstacionamento.class);
        when(t.getStatus()).thenReturn(StatusTicket.FECHADO);

        var p = new Pagamento();
        p.setTicket(t);
        p.setStatus(StatusPagamento.PENDENTE);
        p.setValor(new BigDecimal("10.00"));

        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        assertNotNull(pagamentoService.confirmar(1L));
        assertEquals(StatusPagamento.APROVADO, p.getStatus());
        assertEquals("Pagamento confirmado!", p.getObservacao());
        verify(pagamentoRepository).save(p);
    }

    @Test
    @DisplayName("confirmarDeveLancarNaoEncontradoQuandoPagamentoNaoExiste")
    void confirmarDeveLancarNaoEncontradoQuandoPagamentoNaoExiste() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.empty());

        assertEquals("Pagamento não encontrado!", ex.getMessage());
    }

    static Stream<Arguments> confirmarInvalidos() {
        return Stream.of(
                Arguments.of(StatusPagamento.APROVADO, StatusTicket.FECHADO, "Somente pagamentos PENDENTES podem ser confirmados!"),
                Arguments.of(StatusPagamento.PENDENTE, StatusTicket.ATIVO, "Somente tickets FECHADOS podem ser confirmados!"));
    }

    @ParameterizedTest
    @MethodSource("confirmarInvalidos")
    @DisplayName("confirmarDeveBloquearEstadoInvalido")
    void confirmarDeveBloquearEstadoInvalido(StatusPagamento statusPagamento, StatusTicket statusTicket, String msg) {
        var p = pagamento(statusPagamento, 1L, statusTicket);

        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        var ex = assertThrows(ValidacaoException.class, () -> pagamentoService.confirmar(1L));
        assertEquals(msg, ex.getMessage());
    }

    @Test
    @DisplayName("cancelarDeveCancelarQuandoAdminComMotivo")
    void cancelarDeveCancelarQuandoAdminComMotivo() {
        var admin = usuario(1L, ADMINISTRADOR);
        autenticar(admin);

        var p = pagamento(StatusPagamento.APROVADO, 99L, StatusTicket.FECHADO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        assertNotNull(pagamentoService.cancelar(1L, "  fraude  "));
        assertEquals(StatusPagamento.CANCELADO, p.getStatus());
        assertEquals("Cancelado pelo Administrador: fraude", p.getObservacao());
        verify(pagamentoRepository).save(p);
    }

    static Stream<Arguments> motivoInvalido() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("   "));
    }

    @ParameterizedTest
    @MethodSource("motivoInvalido")
    @DisplayName("cancelarDeveBloquearAdminSemMotivo")
    void cancelarDeveBloquearAdminSemMotivo(String motivo) {
        var admin = usuario(1L, ADMINISTRADOR);
        autenticar(admin);

        var p = pagamento(StatusPagamento.PENDENTE, 99L, StatusTicket.FECHADO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        var ex = assertThrows(ValidacaoException.class, () -> pagamentoService.cancelar(1L, motivo));
        assertEquals("Motivo é obrigatório para cancelamento pelo Administrador!", ex.getMessage());
    }

    @Test
    @DisplayName("cancelarDeveBloquearQuandoJaCancelado")
    void cancelarDeveBloquearQuandoJaCancelado() {
        var admin = usuario(1L, ADMINISTRADOR);
        autenticar(admin);

        var p = pagamento(StatusPagamento.CANCELADO, 99L, StatusTicket.FECHADO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        var ex = assertThrows(ValidacaoException.class, () -> pagamentoService.cancelar(1L, "motivo"));
        assertEquals("Pagamento já está CANCELADO!", ex.getMessage());
    }

    static Stream<Arguments> cancelarNaoAdminCasos() {
        return Stream.of(
                Arguments.of(10L, 99L, StatusPagamento.PENDENTE, "Você não pode cancelar pagamento de outro usuário!"),
                Arguments.of(10L, 10L, StatusPagamento.APROVADO, "Você só pode cancelar pagamentos PENDENTES!"),
                Arguments.of(10L, 10L, StatusPagamento.PENDENTE, null));
    }

    @ParameterizedTest
    @MethodSource("cancelarNaoAdminCasos")
    @DisplayName("cancelarDeveValidarRegrasUsuario")
    void cancelarDeveValidarRegrasUsuario(long idLogado, long idDonoPagamento, StatusPagamento statusPagamento, String msg) {
        var logado = usuario(idLogado, MOTORISTA);
        autenticar(logado);

        var p = pagamento(statusPagamento, idDonoPagamento, StatusTicket.FECHADO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        if (msg != null) {
            var ex = assertThrows(RuntimeException.class, () -> pagamentoService.cancelar(1L, "qualquer"));
            assertEquals(msg, ex.getMessage());
            return;
        }

        assertNotNull(pagamentoService.cancelar(1L, "qualquer"));
        assertEquals(StatusPagamento.CANCELADO, p.getStatus());
        assertEquals("Pagamento cancelado pelo usuário!", p.getObservacao());
        verify(pagamentoRepository).save(p);
    }

    static Stream<Arguments> listarCasos() {
        return Stream.of(
                Arguments.of(ADMINISTRADOR, null, "findAll"),
                Arguments.of(FISCAL, 10L, "findByTicket"),
                Arguments.of(MOTORISTA, 10L, "findByUsuario"));
    }

    @ParameterizedTest
    @MethodSource("listarCasos")
    @DisplayName("listarDeveChamarRepositorioCorreto")
    void listarDeveChamarRepositorioCorreto(TipoUsuario tipo, Long ticketId, String esperado) {
        var logado = usuario(1L, tipo);
        autenticar(logado);

        var pageable = PageRequest.of(0, 10);

        @SuppressWarnings("unchecked")
        Page<Pagamento> pagina = mock(Page.class);

        @SuppressWarnings("unchecked")
        Page<DadosDetalhamentoPagamento> paginaDto = mock(Page.class);

        when(pagina.map(any(Function.class))).thenReturn(paginaDto);

        if ("findAll".equals(esperado)) {
            when(pagamentoRepository.findAll(pageable)).thenReturn(pagina);
        }
        if ("findByTicket".equals(esperado)) {
            when(pagamentoRepository.findByTicket_IdTicket(eq(ticketId), eq(pageable))).thenReturn(pagina);
        }
        if ("findByUsuario".equals(esperado)) {
            when(pagamentoRepository.findByUsuario_IdUsuario(eq(1L), eq(pageable))).thenReturn(pagina);
        }

        assertSame(paginaDto, pagamentoService.listar(ticketId, pageable));

        if ("findAll".equals(esperado)) verify(pagamentoRepository).findAll(pageable);
        if ("findByTicket".equals(esperado)) verify(pagamentoRepository).findByTicket_IdTicket(eq(ticketId), eq(pageable));
        if ("findByUsuario".equals(esperado)) verify(pagamentoRepository).findByUsuario_IdUsuario(eq(1L), eq(pageable));
    }

    static Stream<Arguments> podeCancelarCasos() {
        return Stream.of(
                Arguments.of(ADMINISTRADOR, 1L, true, StatusPagamento.CANCELADO, 99L, true, null),
                Arguments.of(MOTORISTA, 10L, true, StatusPagamento.PENDENTE, 10L, true, null),
                Arguments.of(MOTORISTA, 10L, true, StatusPagamento.PENDENTE, 99L, false, null),
                Arguments.of(MOTORISTA, 10L, true, StatusPagamento.APROVADO, 10L, false, null),
                Arguments.of(MOTORISTA, 10L, false, StatusPagamento.PENDENTE, 10L, null, "Pagamento não encontrado!"));
    }

    @ParameterizedTest
    @MethodSource("podeCancelarCasos")
    @DisplayName("podeCancelarDeveValidarRegras")
    void podeCancelarDeveValidarRegras(TipoUsuario tipo, long idLogado, boolean existe, StatusPagamento status, long idDono, Boolean esperado, String msg) {
        var logado = usuario(idLogado, tipo);
        autenticar(logado);

        if (!existe) {
            when(pagamentoRepository.findById(1L)).thenReturn(Optional.empty());
            assertEquals(msg, ex.getMessage());
            return;
        }

        var p = pagamento(status, idDono, StatusTicket.FECHADO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        assertEquals(esperado, pagamentoService.podeCancelar(1L));
    }

    @Test
    @DisplayName("listarMeusDeveListarPorUsuarioLogado")
    void listarMeusDeveListarPorUsuarioLogado() {
        var logado = usuario(5L, MOTORISTA);
        autenticar(logado);

        var pageable = PageRequest.of(0, 10);

        @SuppressWarnings("unchecked")
        Page<Pagamento> pagina = mock(Page.class);

        @SuppressWarnings("unchecked")
        Page<DadosDetalhamentoPagamento> paginaDto = mock(Page.class);

        when(pagina.map(any(Function.class))).thenReturn(paginaDto);
        when(pagamentoRepository.findByUsuario_IdUsuario(eq(5L), eq(pageable))).thenReturn(pagina);

        assertSame(paginaDto, pagamentoService.listarMeus(pageable));
        verify(pagamentoRepository).findByUsuario_IdUsuario(eq(5L), eq(pageable));
    }
}