package easy.azul.api.service;

import easy.azul.api.dto.Usuario.DadosCadastroUsuario;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.entity.Enum.StatusUsuario;
import easy.azul.api.entity.Enum.TipoUsuario;
import easy.azul.api.entity.Usuario;
import easy.azul.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import static easy.azul.api.entity.Enum.TipoUsuario.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UsuarioService usuarioService;

    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(Usuario usuarioLogado) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuarioLogado);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(ctx);
    }

    private DadosCadastroUsuario motorista(
            String email,
            String telefone,
            String cpf
    ) {
        return new DadosCadastroUsuario(
                "Juliana",
                email,
                "123456",
                cpf,
                null,
                telefone,
                MOTORISTA,
                LocalDate.of(2005, 10, 20),
                null
        );
    }

    private DadosCadastroUsuario empresa(
            String email,
            String telefone,
            String cpf,
            String cnpj
    ) {
        return new DadosCadastroUsuario(
                "Empresa",
                email,
                "123456",
                cpf,
                cnpj,
                telefone,
                EMPRESA,
                null,
                "RAZAO"
        );
    }

    private DadosCadastroUsuario admin(
            TipoUsuario tipo,
            String email,
            String telefone,
            String cpf,
            String cnpj
    ) {
        return new DadosCadastroUsuario(
                tipo == FISCAL ? "Fiscal" : "Admin",
                email,
                "123456",
                cpf,
                cnpj,
                telefone,
                tipo,
                LocalDate.of(2000, 1, 1),
                null
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = TipoUsuario.class,
            names = {"ADMINISTRADOR", "FISCAL"}
    )
    @DisplayName("Não deve permitir cadastro público de administrador ou fiscal")
    void cadastroDeveBloquearAdminFiscal(TipoUsuario tipo) {

        var dados = admin(
                tipo,
                "juliana@gmail.com",
                "47911111111",
                "123.456.789-01",
                null
        );

        var ex = assertThrows(
                ValidacaoException.class,
                () -> usuarioService.cadastrar(dados)
        );

        assertEquals(
                "Tipo de usuário não permitido para cadastro público!",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Deve cadastrar motorista corretamente")
    void cadastroSalvaMotorista() {

        var dados = motorista(
                "motorista@gmail.com",
                "47999999999",
                "310.818.628-07"
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByCpf("31081862807"))
                .thenReturn(false);

        when(passwordEncoder.encode(dados.senha()))
                .thenReturn("hash");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(i -> i.getArgument(0));

        assertNotNull(usuarioService.cadastrar(dados));

        verify(usuarioRepository)
                .save(any(Usuario.class));
    }

    @Test
    @DisplayName("Não deve permitir cadastro com e-mail duplicado")
    void cadastroBloqueiaEmailDup() {

        var dados = motorista(
                "duplicado@gmail.com",
                "47911111111",
                "310.818.628-07"
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(true);

        var ex = assertThrows(
                ValidacaoException.class,
                () -> usuarioService.cadastrar(dados)
        );

        assertEquals(
                "E-mail já cadastrado!",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Não deve permitir cadastro com CPF duplicado")
    void cadastroBloqueiaCpfDup() {

        var dados = motorista(
                "cpf@gmail.com",
                "47922222222",
                "310.818.628-07"
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByCpf("31081862807"))
                .thenReturn(true);

        var ex = assertThrows(
                ValidacaoException.class,
                () -> usuarioService.cadastrar(dados)
        );

        assertEquals(
                "CPF já cadastrado!",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Não deve permitir cadastro de motorista sem CPF")
    void cadastroBloqueiaMotoristaSemCpf() {

        var dados = motorista(
                "semcpf@gmail.com",
                "47933333333",
                null
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        var ex = assertThrows(
                ValidacaoException.class,
                () -> usuarioService.cadastrar(dados)
        );

        assertEquals(
                "CPF é obrigatório para motorista!",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Não deve permitir cadastro de empresa sem CNPJ")
    void cadastroBloqueiaEmpresaSemCnpj() {

        var dados = empresa(
                "cnpjsem@gmail.com",
                "47944444444",
                null,
                null
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        var ex = assertThrows(
                ValidacaoException.class,
                () -> usuarioService.cadastrar(dados)
        );

        assertEquals(
                "CNPJ é obrigatório para empresa!",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Não deve permitir cadastro de empresa com CPF")
    void cadastroBloqueiaEmpresaComCpf() {

        var dados = empresa(
                "cpf@gmail.com",
                "47955555555",
                "31081862807",
                "12.345.678/0001-99"
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        var ex = assertThrows(
                ValidacaoException.class,
                () -> usuarioService.cadastrar(dados)
        );

        assertEquals(
                "Empresa não pode possuir CPF!",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Não deve permitir cadastro com CNPJ duplicado")
    void cadastroBloqueiaCnpjDup() {

        var dados = empresa(
                "cnpjduplicado@gmail.com",
                "47966666666",
                null,
                "12.345.678/0001-99"
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByCnpj("12345678000199"))
                .thenReturn(true);

        var ex = assertThrows(
                ValidacaoException.class,
                () -> usuarioService.cadastrar(dados)
        );

        assertEquals(
                "CNPJ já cadastrado!",
                ex.getMessage()
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = TipoUsuario.class,
            names = {"ADMINISTRADOR", "FISCAL"}
    )
    @DisplayName("Deve cadastrar administrador/fiscal corretamente")
    void cadastroAdminSalva(TipoUsuario tipo) {

        var dados = admin(
                tipo,
                "ok-" + tipo + "@gmail.com",
                "47977777777",
                "123.456.789-01",
                null
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByCpf("12345678901"))
                .thenReturn(false);

        when(passwordEncoder.encode(dados.senha()))
                .thenReturn("hash");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(i -> i.getArgument(0));

        assertNotNull(
                usuarioService.cadastrarAdmin(dados)
        );

        verify(usuarioRepository)
                .save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve seguir fluxo atual ao cadastrar motorista pelo cadastro admin")
    void cadastroAdminComMotoristaSegueFluxoAtual() {

        var dados = admin(
                MOTORISTA,
                "x@gmail.com",
                "47988888888",
                "12345678901",
                null
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        when(usuarioRepository.existsByCpf("12345678901"))
                .thenReturn(false);

        when(passwordEncoder.encode(dados.senha()))
                .thenReturn("hash");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(i -> i.getArgument(0));

        assertNotNull(
                usuarioService.cadastrarAdmin(dados)
        );

        verify(usuarioRepository)
                .save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve validar CNPJ ao cadastrar empresa pelo cadastro admin")
    void cadastroAdminComEmpresaSemCnpjDeveFalhar() {

        var dados = admin(
                EMPRESA,
                "empresa@gmail.com",
                "47988888888",
                null,
                null
        );

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        var ex = assertThrows(
                ValidacaoException.class,
                () -> usuarioService.cadastrarAdmin(dados)
        );

        assertEquals(
                "CNPJ é obrigatório para empresa!",
                ex.getMessage()
        );
    }

    static Stream<Arguments> adminValidacoes() {

        return Stream.of(

                Arguments.of(
                        new DadosCadastroUsuario(
                                "Admin",
                                "adm@gmail.com",
                                "123456",
                                null,
                                null,
                                "47999900000",
                                ADMINISTRADOR,
                                LocalDate.of(2000, 1, 1),
                                null
                        ),
                        "CPF é obrigatório para fiscal/administrador!"
                ),

                Arguments.of(
                        new DadosCadastroUsuario(
                                "Fiscal",
                                "fiscal@gmail.com",
                                "123456",
                                "123.456.789-01",
                                "12.345.678/0001-99",
                                "47999911111",
                                FISCAL,
                                LocalDate.of(2000, 1, 1),
                                null
                        ),
                        "Apenas empresa pode possuir CNPJ!"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("adminValidacoes")
    @DisplayName("Não deve permitir cadastro admin inválido (CPF/CNPJ)")
    void cadastroAdminValidaCpfCnpj(
            DadosCadastroUsuario dados,
            String msg
    ) {

        when(usuarioRepository.existsByEmail(dados.email()))
                .thenReturn(false);

        var ex = assertThrows(
                ValidacaoException.class,
                () -> usuarioService.cadastrarAdmin(dados)
        );

        assertEquals(
                msg,
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Não deve atualizar usuário inexistente")
    void atualizarLancaNaoEncontrado() {

        var dados = mock(
                easy.azul.api.dto.Usuario.DadosAtualizacaoUsuario.class
        );

        when(dados.id())
                .thenReturn(999L);

        when(usuarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        var ex = assertThrows(
                () -> usuarioService.atualizar(dados)
        );

        assertEquals(
                "Usuário não encontrado!",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Deve detalhar usuário existente")
    void detalharRetorna() {

        var usuario = mock(Usuario.class);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        assertNotNull(
                usuarioService.detalhar(1L)
        );
    }

    @Test
    @DisplayName("Deve listar usuários ativos")
    void listarAtivosRetornaPagina() {

        var pageable = PageRequest.of(0, 10);

        when(
                usuarioRepository.findAllByStatus(
                        StatusUsuario.ATIVO,
                        pageable
                )
        ).thenReturn(
                new PageImpl<>(
                        List.of(mock(Usuario.class)),
                        pageable,
                        1
                )
        );

        assertEquals(
                1,
                usuarioService
                        .listarAtivos(pageable)
                        .getTotalElements()
        );
    }

    @Test
    @DisplayName("Deve permitir usuário inativar a si mesmo")
    void inativarPermiteSelf() {

        var logado = mock(Usuario.class);

        when(logado.getTipo())
                .thenReturn(MOTORISTA);

        when(logado.getIdUsuario())
                .thenReturn(10L);

        autenticar(logado);

        var alvo = mock(Usuario.class);

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(alvo));

        when(alvo.getTipo())
                .thenReturn(MOTORISTA);

        assertDoesNotThrow(
                () -> usuarioService.inativar(10L)
        );

        verify(alvo)
                .inativar();

        verify(usuarioRepository)
                .save(alvo);
    }

    static Stream<Arguments> proprioUsuario() {

        return Stream.of(
                Arguments.of(
                        5L,
                        MOTORISTA,
                        5L,
                        true
                ),
                Arguments.of(
                        9L,
                        ADMINISTRADOR,
                        1L,
                        true
                ),
                Arguments.of(
                        2L,
                        MOTORISTA,
                        99L,
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("proprioUsuario")
    @DisplayName("Deve validar se é próprio usuário ou administrador")
    void proprioUsuarioValidaSelfOuAdmin(
            long loggedId,
            TipoUsuario loggedTipo,
            long alvoId,
            boolean esperado
    ) {

        var logado = mock(Usuario.class);

        when(logado.getIdUsuario())
                .thenReturn(loggedId);

        if (loggedId != alvoId) {
            when(logado.getTipo())
                    .thenReturn(loggedTipo);
        }

        autenticar(logado);

        assertEquals(
                esperado,
                usuarioService
                        .proprioUsuarioOuAdministrador(alvoId)
        );
    }
}