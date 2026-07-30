package sobracero.backend.controllers.ong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.dtos.ong.ReservaRequestDto;
import sobracero.backend.dtos.ong.ReservaResponseDto;
import sobracero.backend.services.ong.OngService;
import sobracero.backend.security.JwtUtil;
import sobracero.backend.models.EstadoReserva;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.when;

@WebMvcTest(OngController.class)
@AutoConfigureMockMvc(addFilters = false) // Deshabilitar JWT
public class OngControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OngService ongService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        // Inyectar Principal (Long)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(2L, null, List.of());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void testObtenerCatalogo() throws Exception {
        // Inicializar datos
        DonacionResponseDto donacionMock = new DonacionResponseDto();
        donacionMock.setId(10L);
        donacionMock.setNombre("Tomates Frescos");
        donacionMock.setDescripcion("10kg de tomates");

        // Mocks
        when(ongService.obtenerCatalogo()).thenReturn(List.of(donacionMock));

        // Ejecutar request y validar
        mockMvc.perform(MockMvcRequestBuilders.get("/api/ong/catalogo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].nombre").value("Tomates Frescos"));
    }

    @Test
    public void testCrearReserva() throws Exception {
        // Inicializar datos
        ReservaResponseDto reservaMock = new ReservaResponseDto();
        reservaMock.setId(500L);
        reservaMock.setEstado(EstadoReserva.ACTIVA);
        reservaMock.setCodigoRetiro("A1B2C3");

        // Mocks
        when(ongService.crearReserva(any(ReservaRequestDto.class), anyLong())).thenReturn(reservaMock);

        String requestBody = "{\"donacionId\": 10}";

        // Ejecutar request y validar
        mockMvc.perform(MockMvcRequestBuilders.post("/api/ong/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(500))
                .andExpect(MockMvcResultMatchers.jsonPath("$.codigoRetiro").value("A1B2C3"));
    }
}
