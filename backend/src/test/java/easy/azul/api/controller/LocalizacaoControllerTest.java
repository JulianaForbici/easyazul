package easy.azul.api.controller;

import easy.azul.api.dto.APIMapas.NominatimResponse;
import easy.azul.api.service.MapaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LocalizacaoControllerTest {

    private MockMvc mvc;

    @Mock
    private MapaService service;

    @InjectMocks
    private LocalizacaoController controller;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deveRetornar200() throws Exception {
        NominatimResponse dto = mock(NominatimResponse.class);
        when(service.buscarPorEndereco("vila germanica")).thenReturn(dto);

        mvc.perform(get("/localizacao").param("endereco", "vila germanica")).andExpect(status().isOk());
    }
}