package easy.azul.api.controller;

import easy.azul.api.dto.Veiculo.DadosAtualizacaoVeiculo;
import easy.azul.api.dto.Veiculo.DadosCadastroVeiculo;
import easy.azul.api.dto.Veiculo.DadosDetalhamentoVeiculo;
import easy.azul.api.entity.Veiculo;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.VeiculoRepository;
import easy.azul.api.service.VeiculoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoControllerTest {

    @Mock
    private VeiculoService veiculoService;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private TicketEstacionamentoRepository ticketEstacionamentoRepository;

    @InjectMocks
    private VeiculoController controller;

    @Test
    void cadastrarDeveRetornarOk() {
        var dados = mock(DadosCadastroVeiculo.class);
        var dto = mock(DadosDetalhamentoVeiculo.class);

        when(veiculoService.cadastrar(dados)).thenReturn(dto);

        ResponseEntity<DadosDetalhamentoVeiculo> response = controller.cadastrar(dados);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
        verify(veiculoService).cadastrar(dados);
        verifyNoMoreInteractions(veiculoService);
    }

    @Test
    void listarDeveRetornarPageMapeadaDoRepo() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("placa"));

        var dono1 = mock(easy.azul.api.entity.Usuario.class);
        when(dono1.getIdUsuario()).thenReturn(1L);

        var dono2 = mock(easy.azul.api.entity.Usuario.class);
        when(dono2.getIdUsuario()).thenReturn(2L);

        Veiculo v1 = mock(Veiculo.class);
        when(v1.getDono()).thenReturn(dono1);

        Veiculo v2 = mock(Veiculo.class);
        when(v2.getDono()).thenReturn(dono2);

        Page<Veiculo> pageVeiculos = new PageImpl<>(List.of(v1, v2), pageable, 2);
        when(veiculoRepository.findAll(pageable)).thenReturn(pageVeiculos);

        Page<DadosDetalhamentoVeiculo> result = controller.listar(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        verify(veiculoRepository).findAll(pageable);
        verifyNoMoreInteractions(veiculoRepository);
    }

    @Test
    void detalhardeveRetornarOk() {
        Long id = 10L;

        var dono = mock(easy.azul.api.entity.Usuario.class);
        when(dono.getIdUsuario()).thenReturn(1L);

        Veiculo veiculo = mock(Veiculo.class);
        when(veiculo.getDono()).thenReturn(dono);

        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculo));

        ResponseEntity<DadosDetalhamentoVeiculo> response = controller.detalhar(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(veiculoRepository).findById(id);
        verifyNoMoreInteractions(veiculoRepository);
    }

    @Test
    void detalharDeveLancarValidacaoException() {
        Long id = 999L;

        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        ValidacaoException ex = assertThrows(ValidacaoException.class, () -> controller.detalhar(id));
        assertEquals("Veículo não encontrado", ex.getMessage());

        verify(veiculoRepository).findById(id);
        verifyNoMoreInteractions(veiculoRepository);
    }

    @Test
    void atualizarDeveChamarServiceEDevolverOk() {
        Long id = 5L;
        var dados = mock(DadosAtualizacaoVeiculo.class);
        var dto = mock(DadosDetalhamentoVeiculo.class);

        when(veiculoService.atualizar(id, dados)).thenReturn(dto);

        ResponseEntity<DadosDetalhamentoVeiculo> response = controller.atualizar(id, dados);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(veiculoService).atualizar(id, dados);
        verifyNoMoreInteractions(veiculoService);
    }

    @Test
    void listarMeusVeiculosDeveRepassarPageableParaService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DadosDetalhamentoVeiculo> page = new PageImpl<>(
                List.of(mock(DadosDetalhamentoVeiculo.class)),
                pageable,
                1);

        when(veiculoService.listarDoUsuarioLogado(pageable)).thenReturn(page);

        Page<DadosDetalhamentoVeiculo> result = controller.listarMeusVeiculos(pageable);

        assertSame(page, result);

        verify(veiculoService).listarDoUsuarioLogado(pageable);
        verifyNoMoreInteractions(veiculoService);
    }

    @Test
    void excluirDeveChamarServiceEDevolverNoContent() {
        Long id = 123L;

        ResponseEntity<Void> response = controller.excluir(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(veiculoService).excluir(id);
        verifyNoMoreInteractions(veiculoService);
    }
}