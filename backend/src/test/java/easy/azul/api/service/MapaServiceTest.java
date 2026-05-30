package easy.azul.api.service;

import easy.azul.api.client.MapaClient;
import easy.azul.api.dto.APIMapas.NominatimResponse;
import easy.azul.api.infra.exception.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapaServiceTest {

    @Mock
    MapaClient mapaClient;

    MapaService mapaService;

    @BeforeEach
    void beforeEach() {
        mapaService = new MapaService(mapaClient);
    }

    static Stream<Arguments> casosInvalidos() {
        return Stream.of(
                Arguments.of("retorno null", null),
                Arguments.of("lista vazia", List.of()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosInvalidos")
    @DisplayName("buscarPorEndereco deve bloquear quando não achar")
    void buscarPorEnderecoDeveBloquear(String caso, List<NominatimResponse> retorno) {
        when(mapaClient.buscarEndereco("Rua X")).thenReturn(retorno);

        var ex = assertThrows(ValidacaoException.class, () -> mapaService.buscarPorEndereco("Rua X"));

        assertEquals("Endereço não encontrado", ex.getMessage());
        verify(mapaClient).buscarEndereco("Rua X");
    }

    @ParameterizedTest
    @DisplayName("buscarPorEndereco deve retornar o primeiro item")
    @MethodSource("casosValidos")
    void buscarPorEnderecoDeveRetornarPrimeiro(List<NominatimResponse> retorno) {
        when(mapaClient.buscarEndereco("Rua Y")).thenReturn(retorno);

        var result = mapaService.buscarPorEndereco("Rua Y");

        assertSame(retorno.get(0), result);
        verify(mapaClient).buscarEndereco("Rua Y");
    }

    static Stream<Arguments> casosValidos() {
        var r1 = mock(NominatimResponse.class);
        var r2 = mock(NominatimResponse.class);
        return Stream.of(
                Arguments.of(List.of(r1)),
                Arguments.of(List.of(r1, r2)));
    }
}